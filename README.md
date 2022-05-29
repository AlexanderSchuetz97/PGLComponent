# PGLComponent
PGLComponent is a library that allows for easy inclusion of a Processing 3 3D OpenGL Sketch into a Swing/AWT application.

For more information regarding Processing 3 see: <br>
https://github.com/processing/processing <br>
https://processing.org/


## License
PGLComponent is released under the GNU Lesser General Public License Version 2.1. <br>
A copy of the GNU Lesser General Public License Version 2.1 can be found in the license.txt file. 
This is the same license as Processing 3 itself.

## Usage
Maven:<br>
In addition to your processing dependencies you have to add
````
<dependency>
    <groupId>io.github.alexanderschuetz97</groupId>
    <artifactId>PGLComponent</artifactId>
    <version>1.0</version>
</dependency>
````


Java:
````
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
````

## Info
Full Maven dependencies to get the "MySketch" example working on Java 8:
For Java 11 and Java 17 you will need to use JOGL 2.4 which is as of now
only a RC and not available in the central maven repository. I have
tested it with both java 11 and 17.
````
<dependencies>
    <dependency>
        <groupId>io.github.alexanderschuetz97</groupId>
        <artifactId>PGLComponent</artifactId>
        <version>1.0</version>
    </dependency>
    <dependency>
        <groupId>org.processing</groupId>
        <artifactId>core</artifactId>
        <version>3.3.7</version>
    </dependency>
    <dependency>
        <groupId>org.jogamp.jogl</groupId>
        <artifactId>jogl-all</artifactId>
        <version>2.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.jogamp.gluegen</groupId>
        <artifactId>gluegen-rt</artifactId>
        <version>2.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.jogamp.gluegen</groupId>
        <artifactId>gluegen-rt-main</artifactId>
        <version>2.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.jogamp.jogl</groupId>
        <artifactId>nativewindow</artifactId>
        <version>2.3.2</version>
    </dependency>
</dependencies>
````

### Why should I use this compared to just doing the various hacks described on the internet to get a Processing 3 Surface into a JFrame?
PGLComponent implements a PSurface that renders into a GLAutoDrawable instead of a GLWindow that Processing 3 normally uses.
The GLWindow is the window that always spawns when the Sketch starts and all the hacks do is try to more or less hide this 
window, transfer the "Canvas" to AWT And maybe transfer the input listeners to their new JFrame. The problem with this method is
that it does not work reliably at all, especially on different platforms. 
Since PGLComponent NEVER creates a GLWindow, no window spawns, so you don't have to hide it.

### How do you transfer the content from the GLAutoDrawable to an AWT/Swing Component?
<b>There are multiple ways of doing this: </b><br><br>
One way would be to render into a GLCanvas from JOGL which is shown directly as an AWT component. 
This is what the PGLComponentDirectRenderer and PGLComponentDirectCanvas do. 
This has very poor performance on Linux and (probably) OSX, Windows should be fine.<br><br>
The other option would be to render into a GLOffscreenAutoDrawable that is not directly connected to the screen
and then after it is rendered paint it onto a normal AWT canvas. 
This has the disadvantage of requiring the CPU to do tripple buffering as AWT and the GLOffscreenAutoDrawable may
not have the same frame rate to avoid screen tearing. This is what PGLComponentIndirectRenderer and PGLComponentIndirectCanvas do.
Compared to drawing directly onto the screen this may introduce a small "delay", however at least on Linux and (probably) OSX
the performance lost by using GLCanvas is not worth it at all.
