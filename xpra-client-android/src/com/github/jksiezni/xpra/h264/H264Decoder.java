package com.github.jksiezni.xpra.h264;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import xpra.protocol.packets.DrawPacket;

import java.nio.ByteBuffer;

/**
 * Created by sylvain121 on 29/12/15.
 */
public class H264Decoder {

    public static int FORCE_HARDWARE_DECODE = 1;
    public static int FORCE_SOFTWARE_DECODE = 1;
    private IH264Decoder decoder;


    public H264Decoder (int preference){
        if( preference == FORCE_HARDWARE_DECODE) {

        }

        if(preference == FORCE_SOFTWARE_DECODE) {
            this.decoder = new JCodecDecoder();
        }
    }

    public Bitmap decode(DrawPacket packet) {
            return this.decoder.decode(packet);


    }
}
