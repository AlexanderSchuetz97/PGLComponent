//
// This file is part of PGLComponent.
//
// This file is a heavily modified/merged version of PSurfaceJOGL and PSurfaceAWT
// from the Processing Project - http://processing.org
//
// Copyright holders of PSurfaceJOGL and PSurfaceAWT are:
// Copyright (c) 2012-15 The Processing Foundation
// Copyright (c) 2004-12 Ben Fry and Casey Reas
// Copyright (c) 2001-04 Massachusetts Institute of Technology
//
// The subclass DrawListener is mostly copied from PSurfaceJOGL and all
// mechanisms relating to "listeners" to capture user inputs
// are mostly copied from PSurfaceAWT
//
// The modification/merge was done by Alexander Schütz in 2022
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
//

package io.github.alexanderschuetz97.pglcomponent;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLOffscreenAutoDrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PSurface;
import processing.event.KeyEvent;
import processing.event.MouseEvent;
import processing.opengl.PGL;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implements the Surface that draws to a GLAutoDrawable from JOGL.
 * Also implements various hooks that the Sketch calls.
 * Some hooks that don't make sense for a Component such as "setTitle" are noop.
 */
public class PGLComponentSurface implements PSurface {
    protected final GLProfile profile = GLProfile.get(GLProfile.GL2GL3);
    protected PGLComponentBridge bridge;
    protected GLCapabilities capabilities = new GLCapabilities(profile);
    protected final boolean direct;
    protected GLCanvas canvas;
    protected GLAutoDrawable gldrawable;
    protected GLOffscreenAutoDrawable offscreen;
    protected PGLComponentIndirectRenderer graphics;
    protected PGLComponentInfo info;
    protected PGLComponentPGL pgl;
    protected PApplet sketch;
    protected FPSAnimator animator;
    protected int fps = 60;
    protected boolean paused = false;
    protected boolean stopped = true;

    public PGLComponentSurface(PGLComponentIndirectRenderer graphics, boolean direct) {
        pgl = (PGLComponentPGL) graphics.pgl;
        this.direct = direct;
        this.graphics = graphics;
    }


    public void initOffscreen(PApplet sketch) {
        initFrame(sketch);
    }

    public void initFrame(PApplet sketch) {
        this.sketch = sketch;

        bridge = new PGLComponentBridge();
        info = new PGLComponentInfo();
        info.setRenderingBridge(bridge);
        info.setSketch(sketch);
        info.setSurface(this);


        if (direct) {
            canvas = new GLCanvas(capabilities);
            canvas.setSize(sketch.width, sketch.height);
            canvas.setPreferredSize(new Dimension(sketch.width, sketch.height));
            gldrawable = canvas;
        } else {
            offscreen = GLDrawableFactory.getFactory(profile).createOffscreenAutoDrawable(null, capabilities, null, sketch.width, sketch.height);
            gldrawable = offscreen;
        }

        animator = new FPSAnimator(gldrawable, fps);
        gldrawable.setAnimator(animator);
        info.setDirect(canvas);

        gldrawable.addGLEventListener(new DrawListener());
        addListeners();
    }



    protected void addListeners() {

        info.setMouseListener(new MouseListener() {

            public void mousePressed(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }

            public void mouseReleased(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }

            public void mouseClicked(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }

            public void mouseEntered(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }
        });

        info.setMotionListener(new MouseMotionListener() {

            public void mouseDragged(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }

            public void mouseMoved(java.awt.event.MouseEvent e) {
                nativeMouseEvent(e);
            }
        });

        info.setMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                nativeMouseEvent(e);
            }
        });

        info.setKeyListener(new KeyListener() {

            public void keyPressed(java.awt.event.KeyEvent e) {
                nativeKeyEvent(e);
            }


            public void keyReleased(java.awt.event.KeyEvent e) {
                nativeKeyEvent(e);
            }


            public void keyTyped(java.awt.event.KeyEvent e) {
                nativeKeyEvent(e);
            }
        });

        info.setFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                sketch.focused = true;
                sketch.focusGained();
            }

            public void focusLost(FocusEvent e) {
                sketch.focused = false;
                sketch.focusLost();
            }
        });
    }

    public float getCurrentPixelScale() {
        return 1f;
    }

    public float getPixelScale() {
        if (graphics.pixelDensity == 1) {
            return 1;
        }

        if (PApplet.platform == PConstants.MACOSX) {
            return getCurrentPixelScale();
        }

        return 2;
    }

    public Object getNative() {
        return info;
    }

    public void setTitle(String title) {
        //No
    }

    public void setVisible(boolean visible) {
        //No
    }

    public void setResizable(boolean resizable) {
        //No
    }

    public void setAlwaysOnTop(boolean always) {
        //No
    }

    public void setIcon(PImage icon) {
        //No
    }

    public void placeWindow(int[] location, int[] editorLocation) {
        //?
    }

    public void placePresent(int stopColor) {
        //?
    }

    public void setupExternalMessages() {
        //?
    }

    public void setLocation(int x, int y) {
        //?
    }

    private final AtomicReference<Dimension> resize = new AtomicReference<>();

    protected void checkSketchSize() {
        Dimension dim = resize.getAndSet(null);

        if (dim == null) {
            return;
        }

        sketch.width = dim.width;
        sketch.height = dim.height;
        graphics.setSize(sketch.width, sketch.height);

        if (gldrawable.getSurfaceWidth() != sketch.width || gldrawable.getSurfaceHeight() != sketch.height) {
            if (!direct) {
                //EXPENSIVE CALL only do when needed
                offscreen.setSurfaceSize(sketch.width, sketch.height);
            }
        }
    }

    public void setSize(int width, int height) {
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }

        //Set it next frame...
        resize.set(new Dimension(width, height));
    }

    public void setFrameRate(float fps) {
        this.fps = (int) fps;
        this.fps = Math.max(this.fps, 1);
        PGLComponentExecutor.execute(() -> {
            synchronized (PGLComponentSurface.this) {
                if (!stopped) {
                    animator.stop();
                }
                animator.setFPS(this.fps);
                if (!stopped) {
                    animator.start();
                }

                if (paused) {
                    animator.pause();
                }
            }
        });
    }

    public void setCursor(int kind) {
        //No
    }

    public void setCursor(PImage image, int hotspotX, int hotspotY) {
        //No
    }

    public void showCursor() {
        //No
    }

    public void hideCursor() {
        //No
    }

    public void onDispose() {
        bridge.onDispose();
        gldrawable.destroy();
    }

    public synchronized void startThread() {
        if (!stopped) {
            return;
        }
        stopped = false;
        animator.start();
    }

    public synchronized void pauseThread() {
        if (stopped) {
            return;
        }
        paused = true;

        animator.pause();
    }

    public synchronized void resumeThread() {
        if (!paused) {
            return;
        }
        paused = false;

        animator.resume();
    }

    public synchronized boolean stopThread() {
        if (stopped) {
            return true;
        }
        paused = false;
        stopped = true;
        return animator.stop();
    }

    public synchronized boolean isStopped() {
        return stopped;
    }


    class DrawListener implements GLEventListener {
        public void display(GLAutoDrawable drawable) {
            checkSketchSize();



            if (!sketch.finished) {
                pgl.getGL(drawable);
                int pframeCount = sketch.frameCount;
                sketch.handleDraw();
                if (pframeCount == sketch.frameCount || sketch.finished) {
                    pgl.beginRender();
                    pgl.endRender(sketch.sketchWindowColor());
                }
                PGLComponentIndirectRenderer.completeFinishedPixelTransfers();
            }

            if (sketch.exitCalled()) {
                PGLComponentIndirectRenderer.completeAllPixelTransfers();
                bridge.pull(drawable.getGL(), drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
                sketch.dispose();
                animator.stop();
                drawable.destroy();
                return;
            }

            bridge.pull(drawable.getGL(), drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
        }

        public void dispose(GLAutoDrawable drawable) {

        }

        public void init(GLAutoDrawable drawable) {
            pgl.getGL(drawable);
            pgl.init(drawable);
            sketch.start();

            int c = graphics.backgroundColor;
            pgl.clearColor(((c >> 16) & 0xff) / 255f,
                    ((c >>  8) & 0xff) / 255f,
                    ((c >>  0) & 0xff) / 255f,
                    ((c >> 24) & 0xff) / 255f);
            pgl.clear(PGL.COLOR_BUFFER_BIT);
        }

        public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
            pgl.resetFBOLayer();
            pgl.getGL(drawable);
            float scale = PApplet.platform == PConstants.MACOSX ?
                    getCurrentPixelScale() : getPixelScale();
            setSize((int) (w / scale), (int) (h / scale));
        }
    }

    protected void nativeKeyEvent(java.awt.event.KeyEvent event) {
        int peAction = 0;
        switch (event.getID()) {
            case java.awt.event.KeyEvent.KEY_PRESSED:
                peAction = KeyEvent.PRESS;
                break;
            case java.awt.event.KeyEvent.KEY_RELEASED:
                peAction = KeyEvent.RELEASE;
                break;
            case java.awt.event.KeyEvent.KEY_TYPED:
                peAction = KeyEvent.TYPE;
                break;
        }

        int peModifiers = event.getModifiers() &
                (InputEvent.SHIFT_MASK |
                        InputEvent.CTRL_MASK |
                        InputEvent.META_MASK |
                        InputEvent.ALT_MASK);

        sketch.postEvent(new KeyEvent(event, event.getWhen(),
                peAction, peModifiers,
                event.getKeyChar(), event.getKeyCode()));
    }

    protected void nativeMouseEvent(java.awt.event.MouseEvent nativeEvent) {
        int peCount = nativeEvent.getClickCount();

        int peAction = 0;
        switch (nativeEvent.getID()) {
            case java.awt.event.MouseEvent.MOUSE_PRESSED:
                peAction = MouseEvent.PRESS;
                break;
            case java.awt.event.MouseEvent.MOUSE_RELEASED:
                peAction = MouseEvent.RELEASE;
                break;
            case java.awt.event.MouseEvent.MOUSE_CLICKED:
                peAction = MouseEvent.CLICK;
                break;
            case java.awt.event.MouseEvent.MOUSE_DRAGGED:
                peAction = MouseEvent.DRAG;
                break;
            case java.awt.event.MouseEvent.MOUSE_MOVED:
                peAction = MouseEvent.MOVE;
                break;
            case java.awt.event.MouseEvent.MOUSE_ENTERED:
                peAction = MouseEvent.ENTER;
                break;
            case java.awt.event.MouseEvent.MOUSE_EXITED:
                peAction = MouseEvent.EXIT;
                break;
            case java.awt.event.MouseEvent.MOUSE_WHEEL:
                peAction = MouseEvent.WHEEL;
                peCount = ((MouseWheelEvent) nativeEvent).getWheelRotation();
                break;
        }


        int modifiers = nativeEvent.getModifiers();

        int peModifiers = modifiers &
                (InputEvent.SHIFT_MASK |
                        InputEvent.CTRL_MASK |
                        InputEvent.META_MASK |
                        InputEvent.ALT_MASK);


        int peButton = 0;

        if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            peButton = PConstants.LEFT;
        } else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
            peButton = PConstants.CENTER;
        } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
            peButton = PConstants.RIGHT;
        }

        if (PApplet.platform == PConstants.MACOSX) {
            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                peButton = PConstants.RIGHT;
            }
        }

        sketch.postEvent(new MouseEvent(nativeEvent, nativeEvent.getWhen(),
                peAction, peModifiers,
                (int)(nativeEvent.getX() / getPixelScale()),
                (int)(nativeEvent.getY() / getPixelScale()),
                peButton,
                peCount));
    }
}
