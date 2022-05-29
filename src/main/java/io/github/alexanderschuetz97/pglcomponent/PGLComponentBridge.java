// This file is part of PGLComponent.
// Copyright (c) 2022 Alexander Schütz
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package io.github.alexanderschuetz97.pglcomponent;


import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * OpenGL to AWT Component helper class.
 * Transfers rendered pixels from the OpenGL back buffer to n AWT Components using tripple buffering
 * internally to prevent screen tearing regardless of frame rates.
 */
public class PGLComponentBridge {

    private int width;
    private int height;
    private final ImageWithBuffer[] all;
    private ImageWithBuffer glImage;
    private final AtomicReference<ImageWithBuffer> syncImage = new AtomicReference<>();
    private volatile ImageWithBuffer swingImage;
    private final AtomicInteger pullCounter = new AtomicInteger();
    private final Object nextFrameMutex = new Object();
    private volatile boolean signalNextFrame = false;
    private volatile boolean disposed = false;

    private ByteBuffer glBuffer;
    private int flipStart;
    private int flipRow;
    private int flipSkip;

    public PGLComponentBridge() {
        this.width = -1;
        this.height = -1;
        all = new ImageWithBuffer[3];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Opengl thread frame counter
     */
    protected long frameCounter = Long.MIN_VALUE;

    /**
     * ensures that the buffers match the size opf the canvas.
     */
    protected boolean checkBufferSize(GL gl, int width, int height) {
        if (glBuffer != null && height == this.height && width == this.width) {
            return true;
        }

        all[0] = new ImageWithBuffer(width, height);
        all[1] = new ImageWithBuffer(width, height);
        all[2] = new ImageWithBuffer(width, height);

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                glImage = all[0];
                syncImage.set(all[1]);
                swingImage = all[2];
            } else {
                SwingUtilities.invokeAndWait(() -> {
                    glImage = all[0];
                    syncImage.set(all[1]);
                    swingImage = all[2];
                });
            }
        } catch (Exception e) {
            //TODO log?
            glBuffer = null;
            return false;
        }




        this.width = width;
        this.height = height;

        flipRow = width * 3;

        int[] alignment = new int[1];
        gl.glGetIntegerv(GL.GL_PACK_ALIGNMENT, alignment, 0);

        flipSkip = calculateFlipSkip(flipRow, alignment[0]);

        glBuffer = GLBuffers.newDirectByteBuffer((flipRow+flipSkip)*height);


        flipStart = (flipRow*height)-flipRow;

        return true;
    }

    /**
     * Calculate how many bytes must be skipped at the end of each row based on opengl memory alignment.
     */
    protected int calculateFlipSkip(int flipRow, int alignment) {
        if (alignment == 1) {
            return 0;
        }

        int remainder = flipRow & (alignment - 1);

        if (remainder <= 0) {
            return 0;
        }

        return alignment - remainder;
    }

    public void pull(GL gl, int width, int height) {
        //Check if the image was resized.
        if (!checkBufferSize(gl, width, height)) {
            //Failed to resize buffers.
            //Skip this frame try again next time.
            return;
        }

        //Is there even any component attached to this bridge?
        //If not then there is no point in copying stuff around from and to various buffers
        if (pullCounter.get() <= 0 && !signalNextFrame) {
            return;
        }

        //Read the back buffer from opengl
        GL3 gl3 = gl.getGL3();
        gl3.glReadBuffer(GL.GL_BACK);
        glBuffer.position(0);
        try {
            gl3.glReadPixels(0, 0, width, height, GL.GL_BGR, GL.GL_UNSIGNED_BYTE, glBuffer);
        } catch (Exception ex) {
            //Byte alignment has changed, unlikely unless the GL context was changed...
            glBuffer = null;
            return;
        }

        glBuffer.position(0);

        //Memory layout of the backbuffer is inverted in the y axis.
        //We swap it by filling the buffered image from bottom to top and reading on the backbuffer top to bottom.
        //Each pixel is 3 byte BLUE GREEN RED values. Each row may end with filler bytes that are to be discarded.
        //These filler bytes are used to ensure that each row is aligned with
        //Memory. The alignment is decided on by the GL context.
        for (int x = flipStart; x >= 0; x-=flipRow) {
            //Read a row
            glBuffer.get(glImage.buffer, x, flipRow);
            //Skip the filler bytes
            glBuffer.position(glBuffer.position()+flipSkip);
        }

        //Should never happen in any case we reset all image frame count to invalid when this does happen.
        if (++frameCounter == Long.MIN_VALUE) {
            for (ImageWithBuffer img : all) {
                img.frame = Long.MIN_VALUE;
            }
            frameCounter++;
        }

        glImage.frame = frameCounter;

        glImage = syncImage.getAndSet(glImage);

        if (signalNextFrame) {
            signalNextFrame = false;
            synchronized (nextFrameMutex) {
                nextFrameMutex.notifyAll();
            }
        }
    }

    public void onDispose() {
        synchronized (nextFrameMutex) {
            this.disposed = true;
        }
    }

    /**
     * Fetches the next swing frame. Must always be called in AWT Thread
     */
    protected ImageWithBuffer fetchNextSwingFrame() {
        if (syncImage.get().frame > swingImage.frame) {
            swingImage = syncImage.getAndSet(swingImage);
        }

        return swingImage;
    }

    /**
     * Must be called in the awt thread, or you may get screen tearing in your screenshot
     * and may also cause screen tearing in all AWT components attached to this bridge
     */
    public BufferedImage makeScreenshot() {
        synchronized (nextFrameMutex) {
            if (!disposed) {
                signalNextFrame = true;
                try {
                    nextFrameMutex.wait(5000);
                } catch (InterruptedException e) {
                    //DONT CARE
                }
            }
        }

        ImageWithBuffer myImage = fetchNextSwingFrame();
        BufferedImage image = new BufferedImage(myImage.image.getWidth(), myImage.image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        byte[] buffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(myImage.buffer, 0, buffer, 0, Math.min(myImage.buffer.length, buffer.length));
        return image;

    }



    /**
     * Must be called in the awt thread
     */
    public void push(Graphics graphics) {
        if (glBuffer == null) {
            return;
        }

        ImageWithBuffer myImage = fetchNextSwingFrame();

        if (myImage.frame != Long.MIN_VALUE) {
            graphics.drawImage(myImage.image, 0, 0, null);
        }
    }

    /**
     * Must be called in the awt thread
     */
    public void push(Component component) {
        if (!component.isVisible()) {
            return;
        }

        Dimension size = component.getSize();
        Dimension psize = component.getPreferredSize();
        if (psize.height != height || psize.width != width) {
            Dimension dim = new Dimension(width, height);
            component.setPreferredSize(dim);
            if (size.width <= 0 || size.height <= 0) {
                SwingUtilities.invokeLater(component::revalidate);
            }
        }

        Graphics graphics = component.getGraphics();

        if (graphics != null) {
            push(graphics);
        }
    }

    private static final AtomicLong COUNTER = new AtomicLong();

    protected void asyncPushLoop(Component component, long targetDelay, AtomicBoolean running) {
        pullCounter.incrementAndGet();
        try {
            while(running.get()) {
                try {
                    long s = System.nanoTime();
                    SwingUtilities.invokeAndWait(() -> {
                        if (running.get()) {
                            push(component);
                        }
                    });
                    s = targetDelay - (System.nanoTime() - s);
                    if (s > 0) {
                        TimeUnit.NANOSECONDS.sleep(s);
                    }
                } catch (Exception e) {
                    //TODO LOGGING
                    return;
                }
            }
        } finally {
            pullCounter.decrementAndGet();
        }

    }

    /**
     * Can be called in any thread. Will start a async update loop that will keep
     * repainting the component with image rendered by opengl at a given fps.
     *
     * The actual painting is always done on the AWT EDT Thread.
     */
    public AtomicBoolean start(Component component, int fps) {
        if (fps <= 0) {
            throw new IllegalArgumentException("fps <=0");
        }

        if (fps > 1000) {
            fps = 1000;
        }

        final long trg = 1000000000L / fps;

        final AtomicBoolean running = new AtomicBoolean(true);

        Thread thread = new Thread(() -> asyncPushLoop(component, trg, running));


        thread.start();

        return running;
    }


    /**
     * internal transfer object. Image and its buffer.
     * The data contained is only modified by the opengl thread.
     */
    protected static class ImageWithBuffer {
        protected final BufferedImage image;
        protected final byte[] buffer;
        /**
         * The frame counter. Starts at MIN_VALUE which also means this is an invalid frame.
         * Increased up to max value.
         */
        protected long frame = Long.MIN_VALUE;


        public ImageWithBuffer(int width, int height) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            buffer = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        }
    }
}
