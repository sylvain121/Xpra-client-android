package com.github.jksiezni.xpra.h264;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import xpra.protocol.packets.DrawPacket;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.NoSuchElementException;

class MediaCodecH264DecoderThread extends Thread {

    private final H264Buffer buffer;
    private MediaCodec decoder;
    private Surface surface;
    private int width;
    private int height;
    private byte[] sps;
    private byte[] pps;
    private long startMs;


    public MediaCodecH264DecoderThread(int width, int height, H264Buffer buffer, Surface rendererSurface) {

        this.width = width;
        this.height = height;
        this.buffer = buffer;
        this.surface = rendererSurface;

    }

    private int readData(ByteBuffer buffer, int offset) {
        int length = 0;

        try {
            DrawPacket packet = this.buffer.getNext();
            buffer.clear();
            buffer.put(packet.data);
            length = packet.data.length;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return length;
    }

    private void init(MediaFormat format) {
        if (decoder == null) {
            try {
                decoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.startMs = System.currentTimeMillis();
            decoder.configure(format, surface, null, 0);
            decoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);
        }
    }

    private void getSpsPps(byte[] buffer) {
        int startSps = 0;
        int startPps = 0;
        int end = 0;
        for(int i = 0; i < buffer.length; i++){
            if(buffer[i]== 0 && buffer[i+1] == 0 && buffer[i+2] == 0 && buffer[i+3] == 1) {
                // new nal unit
                if(buffer[i+4] == 103) {
                    // SPS detected
                    startSps = i;
                }
                if(buffer[i+4] == 104) {
                    // PPS detected
                    startPps = i;
                }
            }
            if(buffer[i]== 0 && buffer[i+1] == 0 && buffer[i+2] == 1 && buffer[i+3] == 6 && buffer[i+4] == 5){
                end = i;
            }
        }
        this.sps = Arrays.copyOfRange(buffer, startSps, startPps - 1);
        this.pps = Arrays.copyOfRange(buffer, startPps, end -1);
    }

    @Override
    public void run() {

        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        readFirstPacket();
        //byte[] header_sps = {0, 0, 0, 1, 103, -12, 0, 31, -111, -106, -64, -76, 8, 31, -72, 64, 0, 0, 3, 0, 64, 0, 0, 12, -93, -58, 12, -72};
        //byte[] header_pps = {0, 0, 0, 1, 104, -22, -64, 103, 25, 33, -112};
        format.setByteBuffer("csd-0", ByteBuffer.wrap(this.sps));
        format.setByteBuffer("csd-1", ByteBuffer.wrap(this.pps));
        format.setInteger(MediaFormat.KEY_MAX_WIDTH, width);
        format.setInteger(MediaFormat.KEY_MAX_HEIGHT, height);


        this.init(format);

        if (decoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }
        Log.d(this.getName(), "format : " + decoder.getOutputFormat());
        decoder.start();

        ByteBuffer[] inputBuffers = decoder.getInputBuffers();
        ByteBuffer[] outputBuffers = decoder.getOutputBuffers();
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        boolean isEOS = false;
        long startMs = System.currentTimeMillis();

        while (true) {
            int inIndex = decoder.dequeueInputBuffer(10000);
            if (inIndex >= 0) {
                ByteBuffer buffer = inputBuffers[inIndex];
                int sampleSize = this.readData(buffer, 0);
                Log.d(this.getName(), "sampleSize : " + sampleSize);
                if (sampleSize < 0) {
                    // We shouldn't stop the playback at this point, just pass the EOS
                    // flag to decoder, we will get it again from the
                    // dequeueOutputBuffer
                    Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                    decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    try {
                        Thread.sleep(16);
                        Log.d("DecodeActivity", "waitting data empty buffer");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    long presentationTimeUs = System.currentTimeMillis() - startMs;
                    decoder.queueInputBuffer(inIndex, 0, sampleSize, presentationTimeUs, 0); //TODO sample time is need ?

                }
            }

            int outIndex = decoder.dequeueOutputBuffer(info, 10000);
            Log.d(this.getName(), "bufferInfo : " + info.size);
            switch (outIndex) {
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    Log.d("DecodeActivity", "INFO_OUTPUT_BUFFERS_CHANGED");
                    outputBuffers = decoder.getOutputBuffers();
                    break;
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    format = decoder.getOutputFormat();
                    Log.d("DecodeActivity", "New format " + format);
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                    break;
                default:
                    ByteBuffer buffer = outputBuffers[outIndex];
                    //Log.v("DecodeActivity", "We can't use this buffer but render it due to the API limit, " + buffer);

                    // We use a very simple clock to keep the video FPS, or the video
                    // playback will be too fast
                    try {
                        sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    decoder.releaseOutputBuffer(outIndex, true);
                    break;
            }
            // All decoded frames have been rendered, we can stop playing now
            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        decoder.stop();
        decoder.release();
    }

    private void readFirstPacket() {
        try {
            DrawPacket packet = this.buffer.getNext();
            getSpsPps(packet.data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release() {

    }
}