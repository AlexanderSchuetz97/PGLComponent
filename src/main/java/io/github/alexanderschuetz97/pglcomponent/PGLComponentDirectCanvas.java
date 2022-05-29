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

import com.jogamp.opengl.awt.GLCanvas;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;

/**
 * This component instructs OpenGL to directly draw to a component.
 * This is only advisable on Windows because the
 * performance on Linux and OSX is terrible
 * (OpenGL renders in the AWT event thread on Linux/OSX causing massive lag in all AWT components)
 *
 * On Windows the latency is lower compared to PGLIndirectCanvas.
 *
 * Note: The sketch has no say in how big the canvas is.
 * All depends on the layout manager that layouts this component.
 * The preferred size is populated with the width and height passed on by the sketch tho.
 * The layout manager however is free to ignore this preference and depending on the layout is guaranteed to not care.
 * The sketches size values are adjusted however after the layout manager has decided to layout the component.
 */
public class PGLComponentDirectCanvas extends JPanel {

    private final PGLComponentInfo info;
    private final GLCanvas child;


    public PGLComponentDirectCanvas(PApplet sketch) {
        this.setVisible(true);
        this.setLayout(new BorderLayout());
        this.info = PGLComponentIndirectRenderer.infoFromSketch(sketch);

        child = info.getDirect();
        if (child == null) {
            throw new IllegalArgumentException("Sketch does not use the PGLDirectComponentRenderer");
        }
        if (child.getParent() != null) {
            throw new IllegalStateException("Only 1 PGLDirectCanvas per Sketch allowed!");
        }

        this.setPreferredSize(child.getPreferredSize());

        add(child);

        this.addHierarchyListener(this::hierarchyChanged);
        child.addMouseListener(info.getMouseListener());
        child.addFocusListener(info.getFocusListener());
        child.addMouseMotionListener(info.getMotionListener());
        child.addMouseWheelListener(info.getMouseWheelListener());
        child.addKeyListener(info.getKeyListener());
    }

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
                //Fix inital size of canvas
                SwingUtilities.invokeLater(this::revalidate);
                return;
            }

            if (isDisplayable()) {
                return;
            }

            dispose();
        });
    }

    public BufferedImage screenshot() {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not AWT Event Queue Thread!");
        }

        return info.getRenderingBridge().makeScreenshot();
    }

    public void dispose() {
        dead = true;
        try {
            child.destroy();
            info.getSketch().dispose();
        } finally {
            remove(child);
        }
    }
}
