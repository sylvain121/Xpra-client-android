package com.github.jksiezni.xpra.h264;

import android.graphics.Bitmap;
import xpra.protocol.packets.DrawPacket;

/**
 * Created by sylvain on 29/12/15.
 */
public interface IH264Decoder {

    Bitmap decode(DrawPacket packet);
}
