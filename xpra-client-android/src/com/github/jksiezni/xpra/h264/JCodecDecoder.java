package com.github.jksiezni.xpra.h264;

import android.graphics.Bitmap;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import xpra.protocol.packets.DrawPacket;

import java.nio.ByteBuffer;

/**
 * Created by sylvain121 on 29/12/15.
 */
public class JCodecDecoder  implements IH264Decoder {


    @Override
    public Bitmap decode(DrawPacket packet) {
        org.jcodec.codecs.h264.H264Decoder decoder = new org.jcodec.codecs.h264.H264Decoder();
        Picture out = Picture.create(packet.x, packet.y, ColorSpace.YUV420);
        Picture real = decoder.decodeFrame(ByteBuffer.wrap(packet.data), out.getData());
        return AndroidUtil.toBitmap(real);
    }
}
