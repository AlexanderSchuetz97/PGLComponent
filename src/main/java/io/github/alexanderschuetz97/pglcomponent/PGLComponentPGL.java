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


import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PJOGL;

/**
 *  This PJOGL makes some protected methods public and fixes some unchecked casts to the native PJOGL surface.
 */
public class PGLComponentPGL extends PJOGL {
    public PGLComponentPGL(PGraphicsOpenGL pg) {
        super(pg);
    }

    @Override
    public float getPixelScale() {
        PGLComponentSurface surf = (PGLComponentSurface) sketch.getSurface();
        if (surf == null) {
            return graphics.pixelDensity;
        }
        return surf.getPixelScale();
    }


    @Override
    protected void beginRender() {
        super.beginRender();
    }

    @Override
    protected void endRender(int windowColor) {
        super.endRender(windowColor);
    }
}
