/**
 *
 */
package com.github.jksiezni.xpra.client;

import java.util.ArrayList;
import java.util.List;

import android.view.*;
import com.github.jksiezni.xpra.h264.H264Decoder;
import xpra.client.XpraWindow;
import xpra.protocol.packets.DrawPacket;
import xpra.protocol.packets.NewWindow;
import xpra.protocol.packets.WindowIcon;
import xpra.protocol.packets.WindowMetadata;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author Jakub Księżniak
 */
public class AndroidXpraWindow extends XpraWindow implements OnTouchListener, OnKeyListener {

    private final TextureView textureView;
    private final Handler uiHandler;

    private final Renderer renderer;
    private H264Decoder decoder;

    private final List<XpraWindowListener> listeners = new ArrayList<>();

    // window properties
    private String title;
    private Bitmap icon;
    private int currentWindowWidth = 0;
    private int currentWindowsHeight = 0;
    private SurfaceTexture rendererSurface;

    public AndroidXpraWindow(NewWindow wnd, Context context) {
        super(wnd);
        this.renderer = new Renderer(this);
        this.uiHandler = new Handler(context.getMainLooper());
        this.textureView = new TextureView(context);
        this.textureView.setSurfaceTextureListener(renderer);
        this.textureView.setOnTouchListener(this);
        this.textureView.setOnKeyListener(this);
        this.textureView.setPivotX(0);
        this.textureView.setPivotY(0);

    }

    @Override
    protected void onStart(NewWindow wndPacket) {
        super.onStart(wndPacket);
        Log.i(getClass().getSimpleName(), "onStart() windowId=" + getId());
        final LayoutParams params = wndPacket.isOverrideRedirect() ?
                buildExactParams(wndPacket) :
                buildFullscreenParams(wndPacket);
        textureView.setLayoutParams(params);
    }

    private LayoutParams buildExactParams(final NewWindow wndPacket) {
        final LayoutParams params = new RelativeLayout.LayoutParams(wndPacket.getWidth(), wndPacket.getHeight());
        params.leftMargin = wndPacket.getX();
        params.topMargin = wndPacket.getY();
        return params;
    }

    private LayoutParams buildFullscreenParams(final NewWindow wndPacket) {
        final LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        return params;
    }

    @Override
    protected void onStop() {
        Log.i(getClass().getSimpleName(), "onStop() windowId=" + getId());
        if(this.decoder != null) {
            this.decoder.stopDecode();
        }

    }

    @Override
    protected void onMetadataUpdate(WindowMetadata metadata) {
        super.onMetadataUpdate(metadata);
        final String title = metadata.getAsString("title");
        if (title != null) {
            this.title = title;
        }
        fireOnMetadataChanged();
    }

    @Override
    protected void onIconUpdate(WindowIcon windowIcon) {
        super.onIconUpdate(windowIcon);
        if (windowIcon.encoding == null) {
            return;
        }
        switch (windowIcon.encoding) {
            case h264:
                // TODO ?
            case png:
            case pngL:
            case pngP:
            case jpeg:
                icon = BitmapFactory.decodeByteArray(windowIcon.data, 0, windowIcon.data.length);
                fireOnIconChanged();
                break;

            default:
                break;
        }
    }

    @Override
    public String toString() {
        return getTitle();
    }

    public String getTitle() {
        return title != null ? title : "Undefined";
    }

    public Drawable getIconDrawable() {
        if (icon == null) {
            return null;
        }
        final BitmapDrawable drawable = new BitmapDrawable(textureView.getResources(), icon);
        drawable.setBounds(0, 0, icon.getWidth(), icon.getHeight());
        return drawable;
    }

    public View getView() {
        return textureView;
    }

    public void addOnMetadataListener(XpraWindowListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    protected void fireOnMetadataChanged() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (XpraWindowListener l : listeners) {
                        l.onMetadataChanged(AndroidXpraWindow.this);
                    }
                }
            }
        });
    }
    protected void fireOnIconChanged() {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (listeners) {
                    for (XpraWindowListener l : listeners) {
                        l.onIconChanged(AndroidXpraWindow.this);
                    }
                }
            }
        });
    }



    @Override
    public void draw(DrawPacket packet) {

        if (packet.encoding == null) {
            return;
        }

        try {

            switch (packet.encoding) {
                case h264:
                    if( rendererSurface != null  && decoder == null ){
                        this.decoder = H264Decoder.create(packet.w, packet.h, new Surface(this.rendererSurface));
                    }
                    /*if(this.currentWindowWidth != packet.w || this.currentWindowsHeight != packet.h) {
                        Log.d(this.getClass().getName(), "New Decoder Factory");
                        this.currentWindowWidth = packet.w;
                        this.currentWindowsHeight = packet.h;
                        this.decoder = H264Decoder.create(packet.w, packet.h, new Surface(this.rendererSurface));
                    }*/
                    if( this.decoder != null) {
                        this.decoder.getBuffer().addPacketToQueue(packet);
                    }

                    break;
                case png:
                case pngL:
                case pngP:
                case jpeg:
                    /*Rect dirty = new Rect(packet.x, packet.y, packet.x + packet.w, packet.y + packet.h);
                    Canvas canvas = textureView.lockCanvas(dirty);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(packet.data, 0, packet.data.length);
                    canvas.drawBitmap(bitmap, packet.x, packet.y, null);
                    textureView.unlockCanvasAndPost(canvas);
                    bitmap.recycle();
                    break;*/

                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        event.offsetLocation(v.getX(), v.getY());
        final int x = (int) Math.max(event.getX(), 0);
        final int y = (int) Math.max(event.getY(), 0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setFocused(true);
                mouseAction(1, true, x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                mouseAction(1, true, x, y);
                break;
            case MotionEvent.ACTION_UP:
                mouseAction(1, false, x, y);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        System.out.println(event);
        if (event.isSystem()) {
            System.out.println("isSystem event");
            return false;
        }
        System.out.println(event.getUnicodeChar());
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), true);
                break;

            case KeyEvent.ACTION_UP:
                keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), false);
            default:
                break;
        }
        return true;
    }

    public void setSurfaceTexture(SurfaceTexture surface, int width, int height) {
        this.rendererSurface = surface;
    }

    public void close() {
        closeWindow();
    }

    public void setQuality() {

    }

    public void setMinQuality() {

    }

    public void setSpeed() {

    }

    public void setMinSpeed() {

    }

    public void setH264Decoder(H264Decoder h264Decoder) {
        this.decoder = h264Decoder;
    }

    private class Renderer extends HandlerThread implements SurfaceTextureListener {


        private final AndroidXpraWindow xpraWindow;

        public Renderer(AndroidXpraWindow androidXpraWindow) {
            super("Renderer");
            this.xpraWindow = androidXpraWindow;

        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.i(getClass().getSimpleName(), "onSurfaceTextureAvailable(): " + width + "x" + height);
            this.xpraWindow.setSurfaceTexture(surface, width, height);
            final int x = (int) getView().getX();
            final int y = (int) getView().getY();
            mapWindow(x, y, width, height);
            setFocused(true);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.i(getClass().getSimpleName(), "onSurfaceTextureSizeChanged(): " + width + "x" + height);
            final int x = (int) getView().getX();
            final int y = (int) getView().getY();
            mapWindow(x, y, width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.i(getClass().getSimpleName(), "onSurfaceTextureDestroyed(): ");
            setFocused(false);
            unmapWindow();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.i(getClass().getSimpleName(), "onSurfaceTextureUpdated(): ");
        }

    }

    public interface XpraWindowListener {
        void onMetadataChanged(AndroidXpraWindow window);

        void onIconChanged(AndroidXpraWindow window);
    }

}
