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
        this.buffer.add(packet);
    }

    public DrawPacket getNext() throws InterruptedException {
        return this.buffer.take();
    }
}
