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

public class TestClass {

    public static void main(String[] args) {
        MyApplet app = new MyApplet();

        JFrame frame = new JFrame();
        frame.setSize(800, 400);
        frame.setLayout(new GridLayout(1, 2));
        //frame.setLayout(new FlowLayout());
        frame.setVisible(true);
        frame.add(new PGLComponentIndirectCanvas(app, true, true));

        //frame.add(new PGLDirectCanvas(app));
        frame.add(new PGLComponentIndirectCanvas(app, false, false));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public static class MyApplet extends PApplet {
        public void settings(){
            size(704, 681, PGLComponentIndirectRenderer.class.getName());

            smooth(4);
        }

        public void setup() {
            frameRate(20);
            ortho();
        }


        int z = 1000;
        int x = 1000;

        float zoom = 0;

        int lastMouseY = -1;
        int lastMouseX = -1;
        int lastMouseYDiff = 0;
        int lastMouseXDiff = 0;

        boolean leftMousePressed = false;
        boolean rightMousePressed = false;

        public void draw(){
            lastMouseYDiff = mouseY-lastMouseY;
            lastMouseXDiff = mouseX-lastMouseX;
            lastMouseX = mouseX;
            lastMouseY = mouseY;

            if (leftMousePressed) {
                z+=lastMouseYDiff*10;

                System.out.println(x);
            }


            if (z > 2000) {
                z = 2000;
            }

            if (z < 0) {
                z = 0;
            }

            if (x < 0) {
                x = 0;
            }

            if (x > 2000) {
                x = 2000;
            }


            background(frameCount % 64);

            camera();
            perspective();

            pushMatrix();

            camera(x,
                    1000, z, 0, 0, 0, 0, 0, -1f);

            scale(zoom);
            strokeWeight(2f);
            stroke(color(255,0,0));
            line(0,0,0,1000,0,0);
            stroke(color(0,255,0));
            line(0,0,0,0,1000,0);
            stroke(color(0,0,255));
            line(0,0,0,0,0,1000);
            stroke(color(0,255,255));
            line(0,1000,0,1000,0,0);
            popMatrix();


            stroke(color(255, 0, 255));
            line(0, 0, width, height);

            stroke(255);


            for (int i = 0; i < height; i+= 10) {
                line(0, i, 100, i);
            }



            text("Test " + height + " " + width, 0, 10);

            text("Test " + height + " " + width, 100, 100);
        }

        @Override
        public void keyPressed() {
            if (keyCode == UP) {
                //setSize(400, height+1);
                zoom+=0.1f;
            }

            if (keyCode == DOWN) {
                //setSize(400, height-1);
                zoom-=0.1f;
            }


            System.out.println(zoom);
        }

        @Override
        public void mousePressed() {
            if (mouseButton == LEFT) {
                leftMousePressed = true;
            }

            if (mouseButton == RIGHT) {
                rightMousePressed = true;
            }
        }

        public void mouseReleased() {
            if (mouseButton == LEFT) {
                leftMousePressed = false;
            }

            if (mouseButton == RIGHT) {
                rightMousePressed = false;
            }
        }

    }
}
