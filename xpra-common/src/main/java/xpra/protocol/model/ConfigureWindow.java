package xpra.protocol.model;

import java.util.Collection;

public class ConfigureWindow extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;

	public ConfigureWindow(int windowId, int x, int y, int width, int height) {
		super("configure-window");
		this.windowId = windowId;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(x);
		elems.add(y);
		elems.add(width);
		elems.add(height);
	}

}
