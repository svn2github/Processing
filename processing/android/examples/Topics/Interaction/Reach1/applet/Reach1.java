import processing.core.*; 

import java.applet.*; 
import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Reach1 extends PApplet {

/**
 * Reach 1. 
 * Based on code from Keith Peters (www.bit-101.com)
 * 
 * The arm follows the position of the mouse by
 * calculating the angles with atan2(). 
 */

 
float x = 100;
float y = 100;
float x2 = 100;
float y2 = 100;
float segLength = 30;

public void setup() {
  size(200, 200);
  smooth(); 
  strokeWeight(20.0f);
  stroke(0, 100);
}

public void draw() {
  background(226);
  
  float dx = mouseX - x;
  float dy = mouseY - y;
  float angle1 = atan2(dy, dx);  
  
  float tx = mouseX - cos(angle1) * segLength;
  float ty = mouseY - sin(angle1) * segLength;
  dx = tx - x2;
  dy = ty - y2;
  float angle2 = atan2(dy, dx);  
  x = x2 + cos(angle2) * segLength;
  y = y2 + sin(angle2) * segLength;
  
  segment(x, y, angle1); 
  segment(x2, y2, angle2); 
}

public void segment(float x, float y, float a) {
  pushMatrix();
  translate(x, y);
  rotate(a);
  line(0, 0, segLength, 0);
  popMatrix();
}


  static public void main(String args[]) {
    PApplet.main(new String[] { "Reach1" });
  }
}
