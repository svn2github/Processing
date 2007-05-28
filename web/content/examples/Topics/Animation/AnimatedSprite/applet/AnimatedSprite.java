import processing.core.*; import java.applet.*; import java.awt.*; import java.awt.image.*; import java.awt.event.*; import java.io.*; import java.net.*; import java.text.*; import java.util.*; import java.util.zip.*; public class AnimatedSprite extends PApplet {/**
 * Animated Sprite (Shifty + Teddy)
 * by <a href="http://www.presstube.com">James Patterson</a>. 
 * 
 * Press the mouse button to change animations.
 * Demonstrates loading, displaying, and animating GIF images.
 * It would be easy to write a program to display 
 * animated GIFs, but would not allow as much control over 
 * the display sequence and rate of display. 
 * 
 * Created 28 January 2003
 */
 
AniSprite animation1, animation2;
float xpos, ypos;
float drag = 30.0f;

public void setup()
{
  size(200,200);
  background(255, 204, 0);
  frameRate(24);
  animation1 = new AniSprite("PT_Shifty_00", 38);
  animation2 = new AniSprite("PT_Teddy_00", 60);
}

public void draw()
{ 
  float difx = mouseX - xpos;
  if(abs(difx) > 1.0f) {
    xpos = xpos + difx/drag;
    xpos = constrain(xpos, 0, width);
  }

  // Display the sprite at the position xpos, ypos
  if(mousePressed) {
    background(153, 153, 0);
    animation1.display(xpos-animation1.getWidth()/2, ypos);
  } else {
    background(255, 204, 0);
    animation2.display(xpos-animation1.getWidth()/2, ypos);
  }
}


// Class for animating GIFs

class AniSprite
{
  PImage[] ani;
  int frame;
  int numFrames;
  
  AniSprite(String imageName, int frameCount) {
    numFrames = frameCount;
    ani = new PImage[numFrames];
    loadImages(imageName);
  }

  public void loadImages(String name) {
    for(int i=0; i<numFrames; i++) {
      String imageName = name + ((i < 10) ? "0" : "") + i + ".gif";
      ani[i] = loadImage(imageName);
    }
  }

  public void display(float xpos, float ypos)
  {
    frame = (frame+1)%numFrames;
    image(ani[frame], xpos, ypos);
  }
  
  public int getWidth() {
    return ani[0].width;
  }

}
static public void main(String args[]) {   PApplet.main(new String[] { "AnimatedSprite" });}}