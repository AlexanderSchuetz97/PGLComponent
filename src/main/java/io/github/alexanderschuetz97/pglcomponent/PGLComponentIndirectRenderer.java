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
import processing.core.PSurface;
import processing.opengl.PGL;
import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;

/**
 * Renderer to use in sketch settings for {@link PGLComponentIndirectCanvas} awt components.
 */
public class PGLComponentIndirectRenderer extends PGraphics3D {

    @Override
    public PSurface createSurface() {
        return surface = new PGLComponentSurface(this, false);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (surface != null) {
            ((PGLComponentSurface) surface).onDispose();
        }
    }

    protected PGL createPGL(PGraphicsOpenGL pg) {
        return new PGLComponentPGL(pg);
    }

    //Originally protected
    public static void completeFinishedPixelTransfers() {
        PGraphicsOpenGL.completeFinishedPixelTransfers();
    }

    //Originally protected
    protected static void completeAllPixelTransfers() {
        PGraphicsOpenGL.completeAllPixelTransfers();
    }

    @Override
    public void setSize(int iwidth, int iheight) {
        super.setSize(iwidth, iheight);
    }

    public static PGLComponentInfo infoFromSketch(PApplet sketch) {
        PSurface surface = sketch.getSurface();
        if (surface == null) {
            PApplet.runSketch(new String[]{""}, sketch);
            surface = sketch.getSurface();
        }

        if (surface == null) {
            throw new IllegalStateException("Sketch has null surface even after calling PApplet.runSketch!");
        }

        Object nativeObj = surface.getNative();
        if (!(nativeObj instanceof PGLComponentInfo)) {
            throw new IllegalStateException("Sketch does not have the GLSwingRenderer! during settings you must call size like this: 'size(?,?, GLSwingRenderer.class.getName())'");
        }

        return (PGLComponentInfo) nativeObj;
    }
}
