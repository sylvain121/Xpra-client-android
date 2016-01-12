package com.github.jksiezni.xpra.h264;

import android.util.Log;
import xpra.protocol.packets.DrawPacket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sylvain121 on 11/01/16.
 */
public class H264Buffer {

    LinkedBlockingQueue<DrawPacket> buffer = new LinkedBlockingQueue<DrawPacket>();


    public void addPacketToQueue(DrawPacket packet) {

        try {
            this.buffer.put(packet);
            //bytesToHex(packet.data, 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public DrawPacket getNext() throws InterruptedException {
        synchronized (buffer) {
            return this.buffer.take();
        }
    }
    private void bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        String result = "";
        for (int i=0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
            result +=" ";
            result +=sb.toString();
        }
        Log.d(this.getClass().getName(), result);
    }
}
