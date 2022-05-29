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

import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This component draws the Sketch onto a canvas using tripple buffering.
 * You can have multiple indirect canvases attached to the same sketch to display the sketch multiple times.
 *
 * You can also choose to ignore the layout manager resizing the component and keep the original canvas size set by the sketch at all times
 * should you choose to do so. In this case the image will be cut off at the right and bottom should it not fit. If the component is larger
 * than the canvas then the canvas is rendered in the top right corner of the component.
 *
 * If you do have multiple components attached to the same sketch then do not set allowLayoutManagerToResizeSketch to true for more than 1 component
 * as the components may then fight over who sets the size of the sketch causing the sketch to constantly resize.
 *
 *
 */
public class PGLComponentIndirectCanvas extends Canvas {

    private final boolean disposeSketchWhenRemoved;
    private final boolean allowLayoutManagerToResizeSketch;
    private final PGLComponentInfo info;
    private int fps = 30;

    public PGLComponentIndirectCanvas(PApplet sketch) {
        this(sketch, true, true);
    }

    public PGLComponentIndirectCanvas(PApplet sketch, boolean disposeSketchWhenRemoved, boolean allowLayoutManagerToResizeSketch) {
        this(PGLComponentIndirectRenderer.infoFromSketch(sketch), disposeSketchWhenRemoved, allowLayoutManagerToResizeSketch);
    }

    protected PGLComponentIndirectCanvas(PGLComponentInfo info, boolean disposeSketchWhenRemoved, boolean allowLayoutManagerToResizeSketch) {
        this.disposeSketchWhenRemoved = disposeSketchWhenRemoved;
        this.allowLayoutManagerToResizeSketch = allowLayoutManagerToResizeSketch;
        this.info = info;
        this.addHierarchyListener(this::hierarchyChanged);
        this.addMouseListener(info.getMouseListener());
        this.addFocusListener(info.getFocusListener());
        this.addMouseMotionListener(info.getMotionListener());
        this.addMouseWheelListener(info.getMouseWheelListener());
        this.addKeyListener(info.getKeyListener());
    }

    @Override
    public void paint(Graphics graphics) {
        //Nothing
    }




    protected AtomicBoolean running = new AtomicBoolean(false);
    protected boolean wasDisplayable = false;
    protected boolean dead = false;
    protected void hierarchyChanged(HierarchyEvent event) {
        if (dead) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (dead) {
                return;
            }

            if (!wasDisplayable) {
                if (!isDisplayable()) {
                    return;
                }

                wasDisplayable = true;
                running.set(false);
                running = info.getRenderingBridge().start(this, fps);
                return;
            }

            if (!isDisplayable()) {
                running.set(false);
                if (disposeSketchWhenRemoved) {
                    dead = true;
                    info.sketch.dispose();
                }
                return;
            }

            if (!running.get()) {
                running = info.getRenderingBridge().start(this, fps);
            }
        });
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (!allowLayoutManagerToResizeSketch || dead) {
            return;
        }
        PApplet sketch = info.getSketch();
        if (height == sketch.height && sketch.width == width) {
            return;
        }

        if (width > 0 && height > 0) {
            info.getSurface().setSize(width, height);
        }
    }

    public void setFps(int fps) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not AWT Event Queue Thread!");
        }
        if (dead) {
            return;
        }

        this.fps = fps;

        if (!running.get()) {
            return;
        }
        running.set(false);
        running = info.getRenderingBridge().start(this, fps);
    }

    public void dispose() {
        dead = true;
        running.set(false);
        if (disposeSketchWhenRemoved) {
            info.sketch.dispose();
        }
    }


    public BufferedImage screenshot() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not AWT Event Queue Thread!");
        }

        return info.getRenderingBridge().makeScreenshot();
    }


}