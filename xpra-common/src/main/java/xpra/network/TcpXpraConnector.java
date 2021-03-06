/**
 * 
 */
package xpra.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xpra.client.XpraClient;
import xpra.network.chunks.HeaderChunk;
import xpra.network.chunks.StreamChunk;
import xpra.protocol.packets.Disconnect;

/**
 * @author Jakub Księżniak
 *
 */
public class TcpXpraConnector extends XpraConnector implements Runnable {
	static final Logger logger = LoggerFactory.getLogger(TcpXpraConnector.class);
	
	private final String host;
	private final int port;
	
	private Thread thread;
	
	public TcpXpraConnector(XpraClient client, String hostname, int port) {
		super(client);
		this.host = hostname;
		this.port = port;
	}

	@Override
	public synchronized boolean connect() {
		if(thread != null) {
			return false;
		}
		thread = new Thread(this);
		thread.start();
		return true;
	}
	
	@Override
	public synchronized void disconnect() {
		if(thread != null) {
			if(!disconnectCleanly()) {
    		thread.interrupt();
			}
			thread = null;
		}
	}

	private boolean disconnectCleanly() {
		final XpraSender s = client.getSender();
		if(s != null) {
			s.send(new Disconnect());
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		Socket socket = null;
		try {
			socket = new Socket(host, port);
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			socket.setKeepAlive(true);
			client.onConnect(new XpraSender(os));
			fireOnConnectedEvent();
			
			StreamChunk reader = new HeaderChunk();
			logger.info("Start Xpra connection...");
			while(!Thread.interrupted() && !client.isDisconnectedByServer()) {
				reader = reader.readChunk(is, this);
			}
			logger.info("Finnished Xpra connection!");
		} catch (IOException e) {
			client.onConnectionError(e);
			fireOnConnectionErrorEvent(e);
		}
		finally {
			if(socket != null) try {
				socket.close();
			} catch (Exception ignored) {}
			if(client.getSender() != null) {
				client.getSender().setClosed(true);
			}
			client.onDisconnect();
			fireOnDisconnectedEvent();
		}
	}

	public boolean isRunning() {
		return thread != null && thread.isAlive();
	}
	
}
