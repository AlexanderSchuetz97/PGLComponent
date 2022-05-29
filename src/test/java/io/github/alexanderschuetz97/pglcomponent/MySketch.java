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

public class MySketch extends PApplet {
    public void settings(){
        //The renderer is important! This rendere is more or less equivalent to P3D
        size(500, 500, PGLComponentIndirectRenderer.class.getName());
    }

    public void draw() {
        //Note: AWT may change the size of your sketch so setting camera and perspective
        //is a good idea! This example does not resize the sketch but a different layout manager may do so
        camera();
        perspective();

        //Your draw routing starts here
        background(0);
        strokeWeight(2f);
        stroke(color(255, 0, 255));
        line(0, 0, width, height);
    }

    // Example Swing/AWT code that references "MySketch"
    // You can also add the component to a JPanel or do whatever you want with it.
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(600, 600);
        frame.setLayout(new FlowLayout());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(new PGLComponentIndirectCanvas(new MySketch()));
    }
}