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

import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

/**
 * Transfer object containing all the info to attach a component to the sketch.
 * It contains all AWT listeners of the sketch as well as the rendering bridge of the sketch that is used to
 * transfer the pixels to the components.
 */
public class PGLComponentInfo {

    protected MouseListener mouseListener;
    protected MouseMotionListener motionListener;
    protected MouseWheelListener mouseWheelListener;
    protected KeyListener keyListener;
    protected FocusListener focusListener;
    protected PGLComponentBridge renderingBridge;
    protected PApplet sketch;
    protected PGLComponentSurface surface;
    protected GLCanvas direct;


    public PGLComponentInfo() {
    }

    public GLCanvas getDirect() {
        return direct;
    }

    public void setDirect(GLCanvas direct) {
        this.direct = direct;
    }

    public PGLComponentSurface getSurface() {
        return surface;
    }

    public void setSurface(PGLComponentSurface surface) {
        this.surface = surface;
    }

    public void setMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public void setMotionListener(MouseMotionListener motionListener) {
        this.motionListener = motionListener;
    }

    public void setMouseWheelListener(MouseWheelListener mouseWheelListener) {
        this.mouseWheelListener = mouseWheelListener;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public void setFocusListener(FocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public void setRenderingBridge(PGLComponentBridge renderingBridge) {
        this.renderingBridge = renderingBridge;
    }

    public void setSketch(PApplet sketch) {
        this.sketch = sketch;
    }

    public MouseListener getMouseListener() {
        return mouseListener;
    }

    public MouseMotionListener getMotionListener() {
        return motionListener;
    }

    public MouseWheelListener getMouseWheelListener() {
        return mouseWheelListener;
    }

    public KeyListener getKeyListener() {
        return keyListener;
    }

    public FocusListener getFocusListener() {
        return focusListener;
    }

    public PGLComponentBridge getRenderingBridge() {
        return renderingBridge;
    }

    public PApplet getSketch() {
        return sketch;
    }
}
