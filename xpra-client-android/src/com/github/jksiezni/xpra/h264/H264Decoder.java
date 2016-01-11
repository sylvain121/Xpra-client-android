package com.github.jksiezni.xpra.h264;

import android.view.Surface;
;

/**
 * Created by sylvain121 on 29/12/15.
 */
public class H264Decoder {

    private final int width;
    private final int height;
    private final Surface rendererSurface;
    private final MediaCodecH264DecoderThread thread;
    private H264Buffer buffer = new H264Buffer();

    public H264Decoder(int width, int height, Surface rendererSurface) {
        this.width = width;
        this.height = height;
        this.rendererSurface = rendererSurface;
        this.thread = new MediaCodecH264DecoderThread(this.width, this.height, this.buffer, this.rendererSurface);
        this.thread.start();

    }

    public static H264Decoder create(int width, int height, Surface renderSurface) {
        return new H264Decoder(width, height, renderSurface);
    }

    public H264Buffer getBuffer() {
        return buffer;
    }

    public void stopDecode() {
        this.thread.release();
    }
}
