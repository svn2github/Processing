import processing.core.*; import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; public class Translate extends PApplet {/**
 * Translate. 
 * 
 * The third parameter to the translate() function
 * sets the offset in the z-axis. Positive values
 * move shapes toward the viewer and negative values 
 * move shapes away from the viewer.
 * 
 * Created 16 January 2003
 */
 
float x, y;
float size = 40.0f;

public void setup() 
{
  size(200, 200, P3D);
  noStroke();
}

public void draw() 
{
  background(102);
  
  x = x + 1.0f;
  
  if (x > width) {
    x = -size;
  } 
  
  translate(x, height/2-size/2, x);
  fill(255);
  rect(-size/2, -size/2, size, size);
  
  translate(x, size, x/2);
  fill(0);
  rect(-size/2, -size/2, size, size);
}
static public void main(String args[]) {   PApplet.main(new String[] { "Translate" });}}