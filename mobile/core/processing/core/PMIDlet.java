package processing.core;

import java.io.*;
import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.rms.*;

/**
 * Part of the Mobile Processing project - http://mobile.processing.org
 *
 * Copyright (c) 2004-05 Francis Li
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author  Francis Li
 */
public abstract class PMIDlet extends MIDlet implements Runnable, CommandListener {    
    public static final int CENTER          = 0;
    public static final int CENTER_RADIUS   = 1;
    public static final int CORNER          = 2;
    public static final int CORNERS         = 3;
    
    public static final int POINTS          = 0;
    public static final int LINES           = 1;
    public static final int LINE_STRIP      = 2;
    public static final int LINE_LOOP       = 3;
    public static final int TRIANGLES       = 4;
    public static final int TRIANGLE_STRIP  = 5;
    public static final int QUADS           = 6;
    public static final int QUAD_STRIP      = 7;
    public static final int POLYGON         = 8;
    
    public static final int UP              = Canvas.UP;
    public static final int DOWN            = Canvas.DOWN;
    public static final int LEFT            = Canvas.LEFT;
    public static final int RIGHT           = Canvas.RIGHT;
    public static final int FIRE            = Canvas.FIRE;    
    public static final int GAME_A          = Canvas.GAME_A;
    public static final int GAME_B          = Canvas.GAME_B;
    public static final int GAME_C          = Canvas.GAME_C;
    public static final int GAME_D          = Canvas.GAME_D;
    public static final int SOFTKEY1        = -6;
    public static final int SOFTKEY2        = -7;
    public static final int SEND            = -10;
    
    public static final String SOFTKEY1_NAME    = "SOFT1";
    public static final String SOFTKEY2_NAME    = "SOFT2";
    public static final String SEND_NAME        = "SEND";
    
    public static final int FACE_SYSTEM         = Font.FACE_SYSTEM;
    public static final int FACE_MONOSPACE      = Font.FACE_MONOSPACE;
    public static final int FACE_PROPORTIONAL   = Font.FACE_PROPORTIONAL;
    
    public static final int STYLE_PLAIN         = Font.STYLE_PLAIN;
    public static final int STYLE_BOLD          = Font.STYLE_BOLD;
    public static final int STYLE_ITALIC        = Font.STYLE_ITALIC;
    public static final int STYLE_UNDERLINED    = Font.STYLE_UNDERLINED;
    
    public static final int SIZE_SMALL          = Font.SIZE_SMALL;
    public static final int SIZE_MEDIUM         = Font.SIZE_MEDIUM;
    public static final int SIZE_LARGE          = Font.SIZE_LARGE;

    public static final int RGB             = 0;
    public static final int HSB             = 1;
    
    protected boolean   pointerPressed;
    protected int       pointerX;
    protected int       pointerY;
    
    protected char      key;
    protected int       keyCode;
    protected boolean   keyPressed;
    
    public static final int     MULTITAP_KEY_SPACE      = 0;
    public static final int     MULTITAP_KEY_UPPER      = 1;
    public static final String  MULTITAP_PUNCTUATION    = ".,?!'\"-_:;/()@&#$%*+<=>^";
        
    protected boolean   multitap;
    protected String    multitapText;
    protected char[]    multitapKeySettings;
    protected char[]    multitapBuffer;
    protected int       multitapBufferIndex;
    protected int       multitapBufferLength;
    protected int       multitapLastEdit;
    protected int       multitapEditDuration;
    protected boolean   multitapIsUpperCase;
    protected String    multitapPunctuation;
    
    protected int       framerate;
    protected int       frameCount;
    
    public PCanvas      canvas;    
    public Command      cmdExit;
    public Command      cmdCustom;
    public Display      display;   
    public int          width;
    public int          height;
    
    private Runtime     runtime;
    private Thread      thread;
    private boolean     setup;
    private boolean     running;
    private boolean     redraw;
    private long        startTime;
    private long        lastFrameTime;
    private int         msPerFrame;
    
    private Calendar    calendar;
    private Random      random;
        
    public static final byte    EVENT_KEY_PRESSED       = 1;
    public static final byte    EVENT_KEY_RELEASED      = 2;
    public static final byte    EVENT_SOFTKEY_PRESSED   = 3;
    public static final byte    EVENT_LIBRARY           = 4;
    public static final byte    EVENT_POINTER_PRESSED   = 5;
    public static final byte    EVENT_POINTER_DRAGGED   = 6;
    public static final byte    EVENT_POINTER_RELEASED  = 7;
    
    private byte[]      events;
    private byte[]      eventsClone;
    private int         eventsLength;
    private int[]       eventValues;
    private int[]       eventValuesClone;
    private Object[]    eventData;
    private Object[]    eventDataClone;
    
    /** Creates a new instance of PMIDlet */
    public PMIDlet() {
        display = Display.getDisplay(this);
        runtime = Runtime.getRuntime();
    }
    
    public final Canvas getCanvas() {
        return canvas;
    }
    
    public final void commandAction(Command c, Displayable d) {
        if (c == cmdExit) {
            exit();
        } else if (c == cmdCustom) {
            enqueueEvent(EVENT_SOFTKEY_PRESSED, 0, cmdCustom.getLabel());
        }
    }
    
    public void softkeyPressed(String softkey) {
    }
    
    public final void softkey(String softkey) {
        if (cmdCustom != null) {
            canvas.removeCommand(cmdCustom);
        }
        if (softkey != null) {
            cmdCustom = new Command(softkey, Command.SCREEN, 2);
            canvas.addCommand(cmdCustom);
        }
    }
    
    public final void exit() {
        try {
            destroyApp(true);
            notifyDestroyed();
        } catch (MIDletStateChangeException msce) {                
        }
    }
    
    protected final void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        running = false;
        destroy();
    }
    
    protected final void pauseApp() {
        running = false;
    }
    
    protected final void startApp() throws MIDletStateChangeException {
        if (canvas == null) {
            cmdExit = new Command("Exit", Command.EXIT, 1);
            
            canvas = new PCanvas(this);
            canvas.addCommand(cmdExit);
            canvas.setCommandListener(this);
            
            width = canvas.getWidth();
            height = canvas.getHeight();
            
            startTime = System.currentTimeMillis();
            msPerFrame = 1;
        
            multitapBuffer = new char[64];
            multitapText = "";
            multitapKeySettings = new char[] { '#', '*' };
            multitapPunctuation = MULTITAP_PUNCTUATION;
            multitapEditDuration = 1000;
            
            events = new byte[8];
            eventsClone = new byte[8];
            eventValues = new int[8];
            eventValuesClone = new int[8];
            eventData = new Object[8];
            eventDataClone = new Object[8];
            
            setup = false;
            running = true;
        }
        redraw = true;
        
        display.setCurrent(canvas);
    }
    
    protected final void start() {
        Thread t = new Thread(this);
        t.start();
    }
    
    public final void run() {
        if (!setup) {
            setup();
            lastFrameTime = startTime - msPerFrame;
            setup = true;
        }        
        try {
            do {
                long currentTime = System.currentTimeMillis();
                int elapsed = Math.max(1, (int) (currentTime - lastFrameTime));
                dequeueEvents();
                if (redraw || (running && (elapsed >= msPerFrame))) {
                    canvas.resetMatrix();
                    draw();
                    runtime.gc();
                    canvas.repaint();
                    canvas.serviceRepaints();
                    lastFrameTime = currentTime;
                    framerate = 1000 / elapsed;
                    frameCount++;

                    redraw = false;
                }
                Thread.yield();
            } while (running || (eventsLength > 0));
        } catch (Throwable t) {
            t.printStackTrace();
            Form form = new Form("Exception");
            form.append(t.toString());
            form.setCommandListener(this);
            form.addCommand(cmdExit);
            display.setCurrent(form);
        }
        thread = null;
    }
    
    public void setup() {
    }
    
    public void suspend()  {
    }

    public void resume() {      
    }    
    
    public void destroy() {        
    }
    
    public void draw() {      
    }
    
    public void libraryEvent(Object library, int event, Object data) {
    }
    
    public final void enqueueLibraryEvent(Object library, int event, Object data) {
        enqueueEvent(EVENT_LIBRARY, event, new Object[] { library, data });
    }
    
    public final void enqueueEvent(byte event, int value, Object data) {
        synchronized (this) {
            eventsLength++;
            if (eventsLength > events.length) {
                byte[] oldEvents = events;
                int[] oldEventValues = eventValues;
                Object[] oldEventData = eventData;
                events = new byte[oldEvents.length * 2];
                eventValues = new int[events.length];
                eventData = new Object[events.length];
                System.arraycopy(oldEvents, 0, events, 0, eventsLength - 1);
                System.arraycopy(oldEventValues, 0, eventValues, 0, eventsLength - 1);
                System.arraycopy(oldEventData, 0, eventData, 0, eventsLength - 1);
            }            
            events[eventsLength - 1] = event;
            eventValues[eventsLength - 1] = value;
            eventData[eventsLength - 1] = data;
        }        
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }
    
    private void dequeueEvents() {
        int length;
        synchronized (this) {
            length = eventsLength;
            eventsLength = 0;
            if (eventsClone.length < length) {
                eventsClone = new byte[events.length];
                eventValuesClone = new int[events.length];
                eventDataClone = new Object[events.length];
            }
            System.arraycopy(events, 0, eventsClone, 0, length);
            System.arraycopy(eventValues, 0, eventValuesClone, 0, length);
            System.arraycopy(eventData, 0, eventDataClone, 0, length);
            for (int i = length - 1; i >= 0; i--) {
                eventData[i] = null;
            }
        }
        for (int i = 0; i < length; i++) {
            switch (eventsClone[i]) {
                case EVENT_KEY_PRESSED:
                    keyPressed(eventValuesClone[i]);
                    break;
                case EVENT_KEY_RELEASED:
                    keyReleased(eventValuesClone[i]);
                    break;
                case EVENT_SOFTKEY_PRESSED:
                    softkeyPressed((String) eventDataClone[i]);
                    break;
                case EVENT_LIBRARY:
                    Object[] objs = (Object[]) eventDataClone[i];
                    libraryEvent(objs[0], eventValuesClone[i], objs[1]);
                    synchronized (objs[0]) {
                        objs[0].notifyAll();
                    }
                    break;
                case EVENT_POINTER_PRESSED:
                    pointerPressed = true;
                    pointerX = eventValuesClone[i] >> 16;
                    pointerY = eventValuesClone[i] & 0xffff;
                    pointerPressed();
                    break;
                case EVENT_POINTER_DRAGGED:
                    pointerX = eventValuesClone[i] >> 16;
                    pointerY = eventValuesClone[i] & 0xffff;
                    pointerDragged();
                    break;
                case EVENT_POINTER_RELEASED:
                    pointerPressed = false;
                    pointerX = eventValuesClone[i] >> 16;
                    pointerY = eventValuesClone[i] & 0xffff;
                    pointerReleased();
                    break;
            }
            eventDataClone[i] = null;
        }
    }
    
    private void key(int keyCode) {        
        //// MIDP 1.0 says the KEY_ values map to ASCII values, but I've seen it
        //// different on some foreign (i.e. Korean) handsets
        if ((keyCode >= Canvas.KEY_NUM0) && (keyCode <= Canvas.KEY_NUM9)) {
            key = (char) ('0' + (keyCode - Canvas.KEY_NUM0));
            this.keyCode = (int) key;
        } else {
            switch (keyCode) {
                case Canvas.KEY_POUND:
                    key = '#';
                    this.keyCode = (int) key;
                    break;
                case Canvas.KEY_STAR:
                    key = '*';
                    this.keyCode = (int) key;
                    break;
                default:
                    String name = canvas.getKeyName(keyCode);
                    if (name.equals(SOFTKEY1_NAME)) {
                        key = 0xffff;
                        this.keyCode = SOFTKEY1;
                    } else if (name.equals(SOFTKEY2_NAME)) {
                        key = 0xffff;
                        this.keyCode = SOFTKEY2;
                    } else if (name.equals(SEND_NAME)) {
                        key = 0xffff;
                        this.keyCode = SEND;
                    } else {
                        key = 0xffff;
                        this.keyCode = canvas.getGameAction(keyCode);
                        if (this.keyCode == 0) {
                            this.keyCode = keyCode;
                        }
                    }
            }
        }        
    }
    
    private void keyPressed(int keyCode) {
        keyPressed = true;        
        
        if (multitap) {
            multitapKeyPressed(keyCode);
        }
        
        key(keyCode);
        keyPressed();
    }
    
    private void keyReleased(int keyCode) {
        char oldKey = key;
        int oldKeyCode = this.keyCode;        
        key(keyCode);
        if ((this.key == oldKey) && (this.keyCode == oldKeyCode)) {       
            keyPressed = false;
        } else {
            this.key = oldKey;
            this.keyCode = oldKeyCode;
        }
        keyReleased();        
    }

    public void keyPressed() {
        
    }
    
    public void keyReleased() {
        
    }

    public void pointerPressed() {
        
    }
    
    public void pointerDragged() {
        
    }
    
    public void pointerReleased() {
        
    }
    
    public final void redraw() {
        redraw = true;
    }
    
    public final void loop() {
        running = true;
    }
    
    public final void noLoop() {
        running = false;
        //// let at least one draw to occur following this command
        redraw = true;
    }    
    
    public final void size(int width, int height) {        
    }
    
    public final void framerate(int fps) {        
        msPerFrame = 1000 / fps;
        if (msPerFrame <= 0) {
            msPerFrame = 1;
        }
    }
    
    public final String getProperty(String property) {
        return System.getProperty(property);
    }
    
    public final String textInput() {
        return textInput("Text Input", null, 256);
    }
    
    public final String textInput(String title, String text, int max) {
        TextInputForm form = new TextInputForm(this, title, text, max, TextField.ANY);
        
        synchronized (this) {
            display.setCurrent(form);
            try {
                wait();
            } catch (InterruptedException ie) {                
            }
        }
        display.setCurrent(canvas);
        
        return form.getString();
    }
    
    public final void multitap() {
        multitap = true;
    }
    
    public final void noMultitap() {
        multitap = false;
    }
    
    public final void multitapClear() {
        multitapBufferIndex = 0;
        multitapBufferLength = 0;
        multitapText = "";
    }
    
    public final void multitapDeleteChar() {
        if (multitapBufferIndex > 0) {
            System.arraycopy(multitapBuffer, multitapBufferIndex, multitapBuffer, multitapBufferIndex - 1, multitapBufferLength - multitapBufferIndex);
            multitapBufferLength--;
            multitapBufferIndex--;
            multitapLastEdit = 0;
        }        
        multitapText = new String(multitapBuffer, 0, multitapBufferLength);
    }
    
    private char multitapUpperKeyPressed(boolean editing, char newChar) {
        multitapIsUpperCase = !multitapIsUpperCase;
        if (editing) {
            if (newChar == multitapKeySettings[MULTITAP_KEY_UPPER]) {
                //// delete the char
                multitapDeleteChar();
                multitapLastEdit = millis();
                newChar = 0;
            } else {
                newChar = multitapKeySettings[MULTITAP_KEY_UPPER];
            }
        } else {
            multitapLastEdit = millis();
        }
        return newChar;
    }
    
    protected final void multitapKeyPressed(int keyCode) {
        boolean editing = (keyCode == this.keyCode) && ((millis() - multitapLastEdit) <= multitapEditDuration);
        char newChar = 0;
        if (editing) {
            newChar = multitapBuffer[multitapBufferIndex - 1];
            if (Character.isUpperCase(newChar)) {
                newChar = Character.toLowerCase(newChar);
            }
        }
        char startChar = 0, endChar = 0, otherChar = 0;
        switch (keyCode) {
            case -8: //// Sun WTK 2.2 emulator
                multitapDeleteChar();
                break;
            case Canvas.KEY_STAR:
                if (multitapKeySettings[MULTITAP_KEY_SPACE] == '*') {
                    startChar = ' '; endChar = ' '; otherChar = '*';
                } else if (multitapKeySettings[MULTITAP_KEY_UPPER] == '*') {
                    newChar = multitapUpperKeyPressed(editing, newChar);
                    editing = (newChar == 0);
                } else {
                    startChar = '*'; endChar = '*'; otherChar = '*';
                    editing = false;
                }
                break;
            case Canvas.KEY_POUND:
                if (multitapKeySettings[MULTITAP_KEY_SPACE] == '#') {
                    startChar = ' '; endChar = ' '; otherChar = '#';
                } else if (multitapKeySettings[MULTITAP_KEY_UPPER] == '#') {
                    newChar = multitapUpperKeyPressed(editing, newChar);
                    editing = (newChar == 0);
                } else {
                    startChar = '#'; endChar = '#'; otherChar = '#';
                    editing = false;
                }
                break;
            case Canvas.KEY_NUM0:
                if (multitapKeySettings[MULTITAP_KEY_SPACE] == '0') {
                    startChar = ' '; endChar = ' '; otherChar = '0';
                } else if (multitapKeySettings[MULTITAP_KEY_UPPER] == '0') {
                    newChar = multitapUpperKeyPressed(editing, newChar);
                    editing = (newChar == 0);
                } else {
                    startChar = '0'; endChar = '0'; otherChar = '0';
                    editing = false;
                }
                break;
            case Canvas.KEY_NUM1:                
                int index = 0;
                if (editing) {
                    index = multitapPunctuation.indexOf(newChar) + 1;
                    if (index == multitapPunctuation.length()) {
                        index = 0;
                    }
                }
                newChar = multitapPunctuation.charAt(index);
                break;
            case Canvas.KEY_NUM2:
                startChar = 'a'; endChar = 'c'; otherChar = '2';
                break;
            case Canvas.KEY_NUM3:
                startChar = 'd'; endChar = 'f'; otherChar = '3';
                break;
            case Canvas.KEY_NUM4:
                startChar = 'g'; endChar = 'i'; otherChar = '4';
                break;
            case Canvas.KEY_NUM5:
                startChar = 'j'; endChar = 'l'; otherChar = '5';
                break;
            case Canvas.KEY_NUM6:
                startChar = 'm'; endChar = 'o'; otherChar = '6';
                break;
            case Canvas.KEY_NUM7:
                startChar = 'p'; endChar = 's'; otherChar = '7';
                break;
            case Canvas.KEY_NUM8:
                startChar = 't'; endChar = 'v'; otherChar = '8';
                break;
            case Canvas.KEY_NUM9:
                startChar = 'w'; endChar = 'z'; otherChar = '9';
                break;
            default:
                int action = canvas.getGameAction(keyCode);
                switch (action) {
                    case Canvas.LEFT:
                        multitapLastEdit = 0;
                        multitapBufferIndex = Math.max(0, multitapBufferIndex - 1);
                        break;
                    case Canvas.RIGHT:
                        multitapLastEdit = 0;
                        multitapBufferIndex = Math.min(multitapBufferLength, multitapBufferIndex + 1);
                        break;
                }
        }
        if (startChar > 0) {
            if (editing) {
                newChar++;
            } else {
                newChar = startChar;
            }
            if (newChar == (otherChar + 1)) {
                newChar = startChar;
            } else if (newChar > endChar) {
                newChar = otherChar;
            }
        }
        if (newChar > 0) {
            if (multitapIsUpperCase) {
                newChar = Character.toUpperCase(newChar);
            }
            if (editing) {
                if (multitapBuffer[multitapBufferIndex - 1] != newChar) {
                    multitapBuffer[multitapBufferIndex - 1] = newChar;
                    multitapLastEdit = millis();
                }
            } else {
                multitapBufferLength++;
                if (multitapBufferLength == multitapBuffer.length) {
                    char[] oldBuffer = multitapBuffer;
                    multitapBuffer = new char[oldBuffer.length * 2];
                    System.arraycopy(oldBuffer, 0, multitapBuffer, 0, multitapBufferIndex);
                    System.arraycopy(oldBuffer, multitapBufferIndex, multitapBuffer, multitapBufferIndex + 1, multitapBufferLength - multitapBufferIndex);
                } else {
                    System.arraycopy(multitapBuffer, multitapBufferIndex, multitapBuffer, multitapBufferIndex + 1, multitapBufferLength - multitapBufferIndex);
                }
                multitapBuffer[multitapBufferIndex] = newChar;
                multitapBufferIndex++;
                multitapLastEdit = millis();
            }
            multitapText = new String(multitapBuffer, 0, multitapBufferLength);
        }
    }
    
    public final int freeMemory() {
        return (int) runtime.freeMemory();
    }
    
    public final int totalMemory() {
        return (int) runtime.totalMemory();
    }
    
    public final boolean isColor() {
        return display.isColor();
    }
    
    public final int numColors() {
        return display.numColors();
    }
    
    public final void point(int x1, int y1) {
        canvas.point(x1, y1);
    }
    
    public final void line(int x1, int y1, int x2, int y2) {
        canvas.line(x1, y1, x2, y2);
    }
    
    public final void triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        canvas.triangle(x1, y1, x2, y2, x3, y3);
    }
    
    public final void quad(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        canvas.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }
    
    public final void rect(int x, int y, int width, int height) {
        canvas.rect(x, y, width, height);
    }
    
    public final void rectMode(int MODE) {     
        canvas.rectMode(MODE);
    }
    
    public final void ellipse(int x, int y, int width, int height) {
        canvas.ellipse(x, y, width, height);
    }
    
    public final void ellipseMode(int MODE) {
        canvas.ellipseMode(MODE);
    }
    
    public final void curve(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        canvas.curve(x1, y1, x2, y2, x3, y3, x4, y4);
    }
    
    public final void bezier(int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4) {
        canvas.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }
    
    public final void strokeWeight(int width) {
        canvas.strokeWeight(width);
    }
    
    public final void beginShape(int MODE) {
        canvas.beginShape(MODE);
    }
    
    public final void endShape() {
        canvas.endShape();
    }
    
    public final void vertex(int x, int y) {
        canvas.vertex(x, y);
    }
    
    public final void curveVertex(int x, int y) {
        canvas.curveVertex(x, y);
    }
    
    public final void bezierVertex(int x1, int y1, int x2, int y2, int x3, int y3) {
        canvas.bezierVertex(x1, y1, x2, y2, x3, y3);
    }
    
    public final void translate(int x, int y) {
        canvas.translate(x, y);
    }
    
    public final void pushMatrix() {
        canvas.pushMatrix();
    }
    
    public final void popMatrix() {
        canvas.popMatrix();
    }
    
    public final void resetMatrix() {
        canvas.resetMatrix();
    }
    
    public final void background(int gray) {
        canvas.background(gray);
    }
    
    public final void background(int value1, int value2, int value3) {
        canvas.background(value1, value2, value3);
    }
    
    public final void background(PImage img) {
        canvas.background(img);
    }
    
    public final void colorMode(int mode) {
        canvas.colorMode(mode);
    }
    
    public final void colorMode(int mode, int range) {
        colorMode(mode, range, range, range);
    }
    
    public final void colorMode(int mode, int range1, int range2, int range3) {
        canvas.colorMode(mode, range1, range2, range3);
    }
    
    public final void colorMode(int mode, int range1, int range2, int range3, int range4) {
        canvas.colorMode(mode, range1, range2, range3, range4);
    }
    
    public final int color(int gray) {
        return canvas.color(gray);
    }
    
    public final int color(int gray, int alpha) {
        return canvas.color(gray, alpha);
    }
    
    public final int color(int value1, int value2, int value3) {
        return canvas.color(value1, value2, value3);
    }
    
    public final int color(int value1, int value2, int value3, int alpha) {
        return canvas.color(value1, value2, value3, alpha);
    }
    
    public final void stroke(int gray) {
        canvas.stroke(gray);
    }
    
    public final void stroke(int value1, int value2, int value3) {
        canvas.stroke(value1, value2, value3);
    }
    
    public final void noStroke() {
        canvas.noStroke();
    }
    
    public final void fill(int gray) {
        canvas.fill(gray);
    }
    
    public final void fill(int value1, int value2, int value3) {
        canvas.fill(value1, value2, value3);
    }
    
    public final void noFill() {
        canvas.noFill();
    }
        
    public final PImage loadImage(String filename) {
        try {
            if (filename.startsWith("http")) {
                PClient client = new PClient();
                client.GET(filename);
                byte[] data = client.readBytes();
                return new PImage(data);
            } else {
                Image img = Image.createImage("/" + filename);
                return new PImage(img);
            }
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public final PImage loadImage(byte[] data) {
        return new PImage(data);
    }

    public final void image(PImage img, int x, int y) {
        canvas.image(img, x, y);
    }

    public final void image(PImage img, int sx, int sy, int swidth, int sheight, int dx, int dy) {
        canvas.image(img, sx, sy, swidth, sheight, dx, dy);
    }
    
    public final void imageMode(int mode) {
        canvas.imageMode(mode);
    }
    
    public final PFont loadFont(String fontname, int color, int bgcolor) {
        try {
            return new PFont(getClass().getResourceAsStream("/" + fontname), color, bgcolor);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public final PFont loadFont(String fontname, int color) {
        return loadFont(fontname, color, 0xffffff);
    }
    
    public final PFont loadFont(String fontname) {
        return loadFont(fontname, 0);
    }
    
    public final PFont loadFont() {
        return new PFont(Font.getDefaultFont());
    }
    
    public final PFont loadFont(int face, int style, int size) {
        return new PFont(Font.getFont(face, style, size));
    }
    
    public final void textFont(PFont font) {
        canvas.textFont(font);
    }
    
    public final void textAlign(int MODE) {
        canvas.textAlign(MODE);
    }
    
    public final void text(String data, int x, int y) {
        canvas.text(data, x, y);
    }
    
    public final void text(String data, int x, int y, int width, int height) {
        canvas.text(data, x, y, width, height);
    }
    
    public final String[] textWrap(String data, int width) {
        return canvas.textWrap(data, width, Integer.MAX_VALUE);
    }
    
    public final String[] textWrap(String data, int width, int height) {
        return canvas.textWrap(data, width, height);
    }
    
    public final int textWidth(String data) {
        return canvas.textWidth(data);
    }
        
    public final int millis() {
        return (int) (System.currentTimeMillis() - startTime);
    }
        
    private void checkCalendar() {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.setTime(new Date());
    }
    
    public final int second() {
        checkCalendar();
        return calendar.get(Calendar.SECOND);
    }
    
    public final int minute() {
        checkCalendar();
        return calendar.get(Calendar.MINUTE);
    }
    
    public final int hour() {
        checkCalendar();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    public final int day() {
        checkCalendar();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    
    public final int month() {
        checkCalendar();
        return calendar.get(Calendar.MONTH);
    }
    
    public final int year() {
        checkCalendar();
        return calendar.get(Calendar.YEAR);        
    }
    
    public final int abs(int value) {
        return Math.abs(value);
    }
    
    public final int max(int value1, int value2) {
        return Math.max(value1, value2);
    }
    
    public final int min(int value1, int value2) {
        return Math.min(value1, value2);
    }
    
    public final int sq(int value) {
        return value * value;
    }
    
    public final int pow(int base, int exponent) {
        int value = 1;
        for (int i = 0; i < exponent; i++) {
            value *= base;
        }
        
        return value;
    }
    
    public final int constrain(int value, int min, int max) {
        return Math.min(Math.max(value, min), max);
    }
    
    public final int random(int value1) {
        return random(0, value1);
    }
    
    public final int random(int value1, int value2) {
        if (random == null) {
            random = new Random();
        }
        int min = Math.min(value1, value2);
        int range = Math.abs(value2 - value1) + 1;
        
        return min + Math.abs((random.nextInt() % range));
    }
    
    public final byte[] loadBytes(String filename) {
        try {
            RecordStore store = null;
            try {
                String name = filename;
                if (name.length() > 32) {
                    name = name.substring(0, 32);
                }
                store = RecordStore.openRecordStore(name, false);
                return store.getRecord(1);
            } catch (RecordStoreNotFoundException rsnfe) {         
            } finally {                
                if (store != null) {
                    store.closeRecordStore();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(filename);
            byte[] result;
            if (is != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead = is.read(buffer);
                while (bytesRead >= 0) {
                    baos.write(buffer, 0, bytesRead);
                    bytesRead = is.read(buffer);
                }
                result = baos.toByteArray();
            } else {
                result = new byte[0];
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {                    
                }
            }
        }
    }
    
    public final String[] loadStrings(String filename) {
        try {
            RecordStore store = null;
            try {
                String name = filename;
                if (name.length() > 32) {
                    name = name.substring(0, 32);
                }
                store = RecordStore.openRecordStore(name, false);
                int numRecords = store.getNumRecords();
                String[] strings = new String[numRecords];
                for (int i = 0; i < numRecords; i++) {
                    strings[i] = new String(store.getRecord(i + 1));
                }
                return strings;
            } catch (RecordStoreNotFoundException rsnfe) {                
            } finally {                
                if (store != null) {
                    store.closeRecordStore();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        Vector v = new Vector();
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(filename);
            if (is != null) {
                Reader r = new InputStreamReader(is);

                int numStrings = 0;

                StringBuffer buffer = new StringBuffer();
                int input = r.read();
                while (true) {
                    if ((input < 0) || (input == '\n')) {
                        String s = buffer.toString().trim();
                        if (s.length() > 0) {
                            numStrings++;
                            v.addElement(s);
                        }
                        buffer.delete(0, Integer.MAX_VALUE);

                        if (input < 0) {
                            break;
                        }
                    } else {
                        buffer.append((char) input);
                    }

                    input = r.read();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {                    
                }
            }
        }
        String[] strings = new String[v.size()];
        v.copyInto(strings);
        
        return strings;
    }
    
    public final void saveBytes(String filename, byte[] data) {
        //// max 32 char names on recordstores
        if (filename.length() > 32) {
            throw new RuntimeException("filename must be 32 characters or less");
        }
        try {            
            //// delete recordstore, if it exists
            try {
                RecordStore.deleteRecordStore(filename);
            } catch (RecordStoreNotFoundException rsnfe) {                
            }
            //// create new recordstore
            RecordStore store = RecordStore.openRecordStore(filename, true);
            store.addRecord(data, 0, data.length);
            store.closeRecordStore();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public final void saveStrings(String filename, String[] strings) {
        //// max 32 char names on recordstores
        if (filename.length() > 32) {
            throw new RuntimeException("filename must be 32 characters or less");
        }
        try {            
            //// delete recordstore, if it exists
            try {
                RecordStore.deleteRecordStore(filename);
            } catch (RecordStoreNotFoundException rsnfe) {                
            }
            //// create new recordstore
            RecordStore store = RecordStore.openRecordStore(filename, true);
            //// add each string as a record
            byte[] data;
            for (int i = 0, length = strings.length; i < length; i++) {
                data = strings[i].getBytes();
                store.addRecord(data, 0, data.length);
            }
            store.closeRecordStore();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    public final void print(boolean data) {
        System.out.print(String.valueOf(data));
    }
    
    public final void print(byte data) {
        System.out.print(String.valueOf(data));
    }
    
    public final void print(char data) {
        System.out.print(String.valueOf(data));
    }
    
    public final void print(int data) {
        System.out.print(String.valueOf(data));
    }
    
    public final void print(Object data) {
        System.out.print(String.valueOf(data));
    }
    
    public final void print(String data) {
        System.out.print(data);
    }
    
    public final void println(boolean data) {
        System.out.println(String.valueOf(data));
    }
    
    public final void println(byte data) {
        System.out.println(String.valueOf(data));
    }
    
    public final void println(char data) {
        System.out.println(String.valueOf(data));
    }
    
    public final void println(int data) {
        System.out.println(String.valueOf(data));
    }
    
    public final void println(Object data) {
        System.out.println(String.valueOf(data));
    }
    
    public final void println(String data) {
        System.out.println(data);
    }
    
    public final int length(boolean[] array) {
        return array.length;
    }
    
    public final int length(byte[] array) {
        return array.length;
    }
    
    public final int length(char[] array) {
        return array.length;
    }
    
    public final int length(int[] array) {
        return array.length;
    }
    
    public final int length(Object[] array) {
        return array.length;
    }
    
    public final String join(String[] anyArray, String separator) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = anyArray.length; i < length; i++) {
            buffer.append(anyArray[i]);
            if (i < (length - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }
    
    public final String join(int[] anyArray, String separator) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = anyArray.length; i < length; i++) {
            buffer.append(anyArray[i]);
            if (i < (length - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();
    }
    
    public final String join(int[] intArray, String separator, int digits) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, length = intArray.length; i < length; i++) {
            buffer.append(nf(intArray[i], digits));
            if (i < (length - 1)) {
                buffer.append(separator);
            }
        }
        return buffer.toString();        
    }
    
    public final String nf(int intValue, int digits) {
        StringBuffer buffer = new StringBuffer();
        for (int j = Integer.toString(intValue).length(); j < digits; j++) {
            buffer.append("0");
        }        
        buffer.append(intValue);
        return buffer.toString();
    }
    
    public final String nfp(int intValue, int digits) {
        StringBuffer buffer = new StringBuffer();
        if (intValue < 0) {
            buffer.append("-");
        } else {
            buffer.append("+");
        }
        buffer.append(nf(intValue, digits));        
        return buffer.toString();
    }
    
    public final String nfs(int intValue, int digits) {
        StringBuffer buffer = new StringBuffer();
        if (intValue < 0) {
            buffer.append("-");
        } else {
            buffer.append(" ");
        }
        buffer.append(nf(intValue, digits));        
        return buffer.toString();
    }
    
    public final String[] split(String str) {
        Vector v = new Vector();
        StringBuffer buffer = new StringBuffer();
        char c;
        boolean whitespace = false;
        for (int i = 0, length = str.length(); i < length; i++ ) {
            c = str.charAt(i);
            switch (c) {
                case '\n':
                case '\r':
                case '\f':
                case '\t':
                case ' ':
                    whitespace = true;
                    break;
                default:
                    if (whitespace) {
                        v.addElement(buffer.toString());
                        buffer.delete(0, buffer.length());
                        
                        whitespace = false;
                    }
                    buffer.append(c);
            }
        }        
        if (buffer.length() > 0) {
            v.addElement(buffer.toString());
        }
        
        String[] tokens = new String[v.size()];
        v.copyInto(tokens);
        
        return tokens;
    }
    
    public final String[] split(String str, String delim) {
        Vector v = new Vector();
        int prevIndex = 0;
        int nextIndex = str.indexOf(delim, prevIndex);
        int delimLength = delim.length();
        while (nextIndex >= 0) {
            v.addElement(str.substring(prevIndex, nextIndex));
            prevIndex = nextIndex + delimLength;
            nextIndex = str.indexOf(delim, prevIndex);
        }
        if (prevIndex < str.length()) {
            v.addElement(str.substring(prevIndex));
        }
        
        String[] tokens = new String[v.size()];
        v.copyInto(tokens);
        
        return tokens;
    }
    
    public final String trim(String str) {
        //// deal with unicode nbsp later
        return str.trim();
    }
    
    public final String[] append(String[] array, String element) {
        String[] old = array;
        int length = old.length;
        array = new String[length + 1];
        System.arraycopy(old, 0, array, 0, length);
        array[length] = element;
        return array;        
    }
    
    public final boolean[] append(boolean[] array, boolean element) {
        boolean[] old = array;
        int length = old.length;
        array = new boolean[length + 1];
        System.arraycopy(old, 0, array, 0, length);
        array[length] = element;
        return array;        
    }
    
    public final byte[] append(byte[] array, byte element) {
        byte[] old = array;
        int length = old.length;
        array = new byte[length + 1];
        System.arraycopy(old, 0, array, 0, length);
        array[length] = element;
        return array;        
    }
    
    public final char[] append(char[] array, char element) {
        char[] old = array;
        int length = old.length;
        array = new char[length + 1];
        System.arraycopy(old, 0, array, 0, length);
        array[length] = element;
        return array;        
    }
    
    public final int[] append(int[] array, int element) {
        int[] old = array;
        int length = old.length;
        array = new int[length + 1];
        System.arraycopy(old, 0, array, 0, length);
        array[length] = element;
        return array;        
    }
    
    public final void arraycopy(Object src, int srcPos, Object dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }
    
    public final String[] concat(String[] array1, String[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        String[] array = new String[length1 + length2];
        System.arraycopy(array1, 0, array, 0, length1);
        System.arraycopy(array2, 0, array, length1, length2);
        return array;
    }
    
    public final boolean[] concat(boolean[] array1, boolean[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        boolean[] array = new boolean[length1 + length2];
        System.arraycopy(array1, 0, array, 0, length1);
        System.arraycopy(array2, 0, array, length1, length2);
        return array;
    }
    
    public final byte[] concat(byte[] array1, byte[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        byte[] array = new byte[length1 + length2];
        System.arraycopy(array1, 0, array, 0, length1);
        System.arraycopy(array2, 0, array, length1, length2);
        return array;
    }
    
    public final char[] concat(char[] array1, char[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        char[] array = new char[length1 + length2];
        System.arraycopy(array1, 0, array, 0, length1);
        System.arraycopy(array2, 0, array, length1, length2);
        return array;
    }
    
    public final int[] concat(int[] array1, int[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;
        int[] array = new int[length1 + length2];
        System.arraycopy(array1, 0, array, 0, length1);
        System.arraycopy(array2, 0, array, length1, length2);
        return array;
    }
    
    public final String[] contract(String[] array, int newSize) {
        int length = array.length;
        if (length > newSize) {
            String[] old = array;
            array = new String[newSize];
            System.arraycopy(old, 0, array, 0, newSize);
        }
        return array;
    }
    
    public final String[] expand(String[] array) {
        return expand(array, array.length * 2);
    }
    
    public final String[] expand(String[] array, int newSize) {
        int length = array.length;
        if (length < newSize) {
            String[] old = array;
            array = new String[newSize];
            System.arraycopy(old, 0, array, 0, length);
        }
        return array;
    }
    
    public final String[] reverse(String[] array) {
        int length = array.length;
        String[] reversed = new String[length];
        for (int i = length - 1; i >= 0; i--) {
            reversed[i] = array[length - i - 1];
        }
        return reversed;
    }
    
    public final String[] shorten(String[] array) {
        String[] old = array;
        int length = old.length - 1;
        array = new String[length];
        System.arraycopy(old, 0, array, 0, length);
        return array;
    }
    
    public final String[] slice(String[] array, int offset) {
        return slice(array, offset, array.length - offset);
    }
    
    public final String[] slice(String[] array, int offset, int length) {
        String[] slice = new String[length];
        System.arraycopy(array, offset, slice, 0, length);
        return slice;
    }
    
    public final String[] splice(String[] array, String value, int index) {
        int length = array.length;
        String[] splice = new String[length + 1];
        System.arraycopy(array, 0, splice, 0, index);
        splice[index] = value;
        System.arraycopy(array, index, splice, index + 1, length - index);
        return splice;
    }
    
    public final String[] splice(String[] array, String[] array2, int index) {
        int length = array.length;
        int length2 = array2.length;
        String[] splice = new String[length + length2];
        System.arraycopy(array, 0, splice, 0, index);
        System.arraycopy(array2, 0, splice, index, length2);
        System.arraycopy(array, index, splice, index + length2, length - index);
        return splice;
    }
    
    //// casting 
    
    public static final char toChar(boolean val) {
        return (val ? 't' : 'f');
    }
    
    public static final char toChar(byte val) {
        return (char) (val & 0xff);
    }
    
    public static final char toChar(int val) {
        return (char) val;
    }
    
    public static final char[] toChar(String val) {
        return val.toCharArray();
    }
    
    public static final char[] toChar(boolean[] val) {
        char[] result = new char[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = val[i] ? 't' : 'f';
        }
        return result;
    }
    
    public static final char[] toChar(byte[] val) {
        char[] result = new char[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = (char) (val[i] & 0xff);
        }
        return result;
    }
    
    public static final char[] toChar(int[] val) {
        char[] result = new char[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = (char) val[i];
        }
        return result;
    }
    
    public static final char[][] toChar(String[] val) {
        char[][] result = new char[val.length][];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = val[i].toCharArray();
        }
        return result;
    }
    
    public static final int toInt(boolean val) {
        return (val ? 1 : 0);
    }
    
    public static final int toInt(byte val) {
        return (val & 0xff);
    }
    
    public static final int toInt(char val) {
        return val;
    }
    
    public static final int toInt(String val) {
        int result = 0;
        try {
            result = Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
        }
        return result;
    }
    
    public static final int[] toInt(boolean[] val) {
        int[] result = new int[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = val[i] ? 't' : 'f';
        }
        return result;
    }
    
    public static final int[] toInt(byte[] val) {
        int[] result = new int[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = (int) (val[i] & 0xff);
        }
        return result;
    }
    
    public static final int[] toInt(char[] val) {
        int[] result = new int[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = (int) val[i];
        }
        return result;
    }
    
    public static final int[] toInt(String[] val) {
        int[] result = new int[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            try {
                result[i] = Integer.parseInt(val[i]);
            } catch (NumberFormatException nfe) {
            }
        }
        return result;
    }
    
    public static final String str(boolean val) {
        return String.valueOf(val);
    }
    
    public static final String str(byte val) {
        return String.valueOf(val);
    }
    
    public static final String str(char val) {
        return String.valueOf(val);
    }
    
    public static final String str(int val) {
        return String.valueOf(val);
    }
    
    public static final String[] str(boolean[] val) {
        String[] result = new String[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = String.valueOf(val[i]);
        }
        return result;
    }
    
    public static final String[] str(byte[] val) {
        String[] result = new String[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = String.valueOf(val[i]);
        }
        return result;
    }
    
    public static final String[] str(char[] val) {
        String[] result = new String[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = String.valueOf(val[i]);
        }
        return result;
    }
    
    public static final String[] str(int[] val) {
        String[] result = new String[val.length];
        for (int i = val.length - 1; i >= 0; i--) {
            result[i] = String.valueOf(val[i]);
        }
        return result;
    }
    
    //// Experimental fixed point math routines here
    
    /** Precision, in number of bits for the fractional part. */
    public static final int FP_PRECISION    = 8;
    /** Convenience constant of the value 1 in fixed point. */
    public static final int ONE             = 1 << FP_PRECISION;
    /** Convenience constant of the value of pi in fixed point. */
    public static final int PI              = (int) ((3.14159265358979323846f) * ONE);
    /** Convenience constant of the value of 2*pi in fixed point. */
    public static final int TWO_PI          = 2 * PI;
    /** Convenience constant of the value of pi/2 in fixed point. */
    public static final int HALF_PI         = PI / 2;
    
    /** Multiplies two fixed point values and returns a fixed point value. */
    public final int mul(int value1, int value2) {
        return (value1 * value2) >> FP_PRECISION;
    }
    
    /** Returns the fixed point quotient from dividing the fixed point dividend by the fixed point divisor. */
    public final int div(int dividend, int divisor) {
        return (dividend << FP_PRECISION) / divisor;
    }
    
    /** Returns the fixed point representation of the specified integer value. */
    public final int itofp(int value1) {
        return value1 << FP_PRECISION;
    }
    
    /** Returns the integer less than or equal to the fixed point value. */
    public final int fptoi(int value1) {
        if (value1 < 0) {
            value1 += ONE - 1;
        }
        return value1 >> FP_PRECISION;
    }
        
    /** Returns the fixed-point square root of a fixed-point value, approximated using Newton's method. */
    public final int sqrt(int value_fp) {
        int prev_fp, next_fp, error_fp, prev;        
        //// initialize previous result
        prev_fp = value_fp;        
        next_fp = 0;
        do {
            prev = prev_fp >> FP_PRECISION;
            if (prev == 0) {                
                break;
            }
            //// calculate a new approximation
            next_fp = (prev_fp + value_fp / prev) / 2;
            if (prev_fp > next_fp) {
                error_fp = prev_fp - next_fp;
            } else {
                error_fp = next_fp - prev_fp;
            }
            prev_fp = next_fp;
        } while (error_fp > ONE);      
        
        return next_fp;
    }
    
    public final int dist(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return sqrt((dx * dx + dy * dy) << FP_PRECISION);
    }
    
    public final int dist_fp(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return sqrt(((dx * dx) >> FP_PRECISION) + ((dy * dy) >> FP_PRECISION));
    }
    
    /** Returns the closest integer fixed-point value less than or equal to the specified fixed point value. */
    public final int floor(int value1) {
        return (value1 >> FP_PRECISION) << FP_PRECISION;
    }
    
    /** Returns the closest integer fixed-point value greater than or equal to the specified fixed point value. */
    public final int ceil(int value1) {
        return ((value1 + ONE - 1) >> FP_PRECISION) << FP_PRECISION;
    }
    
    /** Returns the nearest integer fixed-point value to the specified fixed point value. */
    public final int round(int value1) {
        //// return result
        return ((value1 + (ONE >> 1)) >> FP_PRECISION) << FP_PRECISION;
    }
    
    /** Returns the fixed point radian equivalent to the specified fixed point degree value. */
    public final int radians(int angle) {
        return angle * PI / (180 << FP_PRECISION);
    }
    
    /** Returns the sin of the specified fixed-point radian angle as a fixed point value. */
    public final int sin(int rad) {
        //// convert to degrees
        int index = rad * 180 / PI % 360;
        if (index < 0) {
            index += 360;
        }
        return sin[index];
    }
    
    /** Returns the cos of the specified fixed-point radian angle as a fixed point value. */
    public final int cos(int rad) {
        //// convert to degrees
        int index = (rad * 180 / PI + 90) % 360;
        if (index < 0) {
            index += 360;
        }
        return sin[index];
    }
    
    public final int atan(int value1) {
        int result;
        if (value1 <= ONE) {
            result = div(value1, ONE + mul(((int) (0.28f * ONE)), mul(value1, value1)));
        } else {
            result = HALF_PI - div(value1, (mul(value1, value1) + ((int) (0.28f * ONE))));
        }
        return result;
    }
    
    public final int atan2(int y, int x) {
        int result;
        if ((y == 0) && (x == 0)) {
            result = 0;
        } else if (x > 0) {
            result = atan(div(y, x));
        } else if (x < 0) {
            if (y < 0) {
                result = -(PI - atan(div(-y, -x)));
            } else {
                result = PI - atan(div(y, -x));
            }
        } else {
            if (y < 0) {
                result = -HALF_PI;
            } else {
                result = HALF_PI;
            }
        }
        return result;
    }
    
    /** Lookup table for sin function, indexed by degrees. */
    public static final int[] sin = {
        (int) (0f * ONE),
        (int) (0.0174524064372835f * ONE),
        (int) (0.034899496702501f * ONE),
        (int) (0.0523359562429438f * ONE),
        (int) (0.0697564737441253f * ONE),
        (int) (0.0871557427476582f * ONE),
        (int) (0.104528463267653f * ONE),
        (int) (0.121869343405147f * ONE),
        (int) (0.139173100960065f * ONE),
        (int) (0.156434465040231f * ONE),
        (int) (0.17364817766693f * ONE),
        (int) (0.190808995376545f * ONE),
        (int) (0.207911690817759f * ONE),
        (int) (0.224951054343865f * ONE),
        (int) (0.241921895599668f * ONE),
        (int) (0.258819045102521f * ONE),
        (int) (0.275637355816999f * ONE),
        (int) (0.292371704722737f * ONE),
        (int) (0.309016994374947f * ONE),
        (int) (0.325568154457157f * ONE),
        (int) (0.342020143325669f * ONE),
        (int) (0.3583679495453f * ONE),
        (int) (0.374606593415912f * ONE),
        (int) (0.390731128489274f * ONE),
        (int) (0.4067366430758f * ONE),
        (int) (0.422618261740699f * ONE),
        (int) (0.438371146789077f * ONE),
        (int) (0.453990499739547f * ONE),
        (int) (0.469471562785891f * ONE),
        (int) (0.484809620246337f * ONE),
        (int) (0.5f * ONE),
        (int) (0.515038074910054f * ONE),
        (int) (0.529919264233205f * ONE),
        (int) (0.544639035015027f * ONE),
        (int) (0.559192903470747f * ONE),
        (int) (0.573576436351046f * ONE),
        (int) (0.587785252292473f * ONE),
        (int) (0.601815023152048f * ONE),
        (int) (0.615661475325658f * ONE),
        (int) (0.629320391049837f * ONE),
        (int) (0.642787609686539f * ONE),
        (int) (0.656059028990507f * ONE),
        (int) (0.669130606358858f * ONE),
        (int) (0.681998360062498f * ONE),
        (int) (0.694658370458997f * ONE),
        (int) (0.707106781186547f * ONE),
        (int) (0.719339800338651f * ONE),
        (int) (0.73135370161917f * ONE),
        (int) (0.743144825477394f * ONE),
        (int) (0.754709580222772f * ONE),
        (int) (0.766044443118978f * ONE),
        (int) (0.777145961456971f * ONE),
        (int) (0.788010753606722f * ONE),
        (int) (0.798635510047293f * ONE),
        (int) (0.809016994374947f * ONE),
        (int) (0.819152044288992f * ONE),
        (int) (0.829037572555042f * ONE),
        (int) (0.838670567945424f * ONE),
        (int) (0.848048096156426f * ONE),
        (int) (0.857167300702112f * ONE),
        (int) (0.866025403784439f * ONE),
        (int) (0.874619707139396f * ONE),
        (int) (0.882947592858927f * ONE),
        (int) (0.891006524188368f * ONE),
        (int) (0.898794046299167f * ONE),
        (int) (0.90630778703665f * ONE),
        (int) (0.913545457642601f * ONE),
        (int) (0.92050485345244f * ONE),
        (int) (0.927183854566787f * ONE),
        (int) (0.933580426497202f * ONE),
        (int) (0.939692620785908f * ONE),
        (int) (0.945518575599317f * ONE),
        (int) (0.951056516295154f * ONE),
        (int) (0.956304755963035f * ONE),
        (int) (0.961261695938319f * ONE),
        (int) (0.965925826289068f * ONE),
        (int) (0.970295726275996f * ONE),
        (int) (0.974370064785235f * ONE),
        (int) (0.978147600733806f * ONE),
        (int) (0.981627183447664f * ONE),
        (int) (0.984807753012208f * ONE),
        (int) (0.987688340595138f * ONE),
        (int) (0.99026806874157f * ONE),
        (int) (0.992546151641322f * ONE),
        (int) (0.994521895368273f * ONE),
        (int) (0.996194698091746f * ONE),
        (int) (0.997564050259824f * ONE),
        (int) (0.998629534754574f * ONE),
        (int) (0.999390827019096f * ONE),
        (int) (0.999847695156391f * ONE),
        (int) (1f * ONE),
        (int) (0.999847695156391f * ONE),
        (int) (0.999390827019096f * ONE),
        (int) (0.998629534754574f * ONE),
        (int) (0.997564050259824f * ONE),
        (int) (0.996194698091746f * ONE),
        (int) (0.994521895368273f * ONE),
        (int) (0.992546151641322f * ONE),
        (int) (0.99026806874157f * ONE),
        (int) (0.987688340595138f * ONE),
        (int) (0.984807753012208f * ONE),
        (int) (0.981627183447664f * ONE),
        (int) (0.978147600733806f * ONE),
        (int) (0.974370064785235f * ONE),
        (int) (0.970295726275996f * ONE),
        (int) (0.965925826289068f * ONE),
        (int) (0.961261695938319f * ONE),
        (int) (0.956304755963036f * ONE),
        (int) (0.951056516295154f * ONE),
        (int) (0.945518575599317f * ONE),
        (int) (0.939692620785908f * ONE),
        (int) (0.933580426497202f * ONE),
        (int) (0.927183854566787f * ONE),
        (int) (0.92050485345244f * ONE),
        (int) (0.913545457642601f * ONE),
        (int) (0.90630778703665f * ONE),
        (int) (0.898794046299167f * ONE),
        (int) (0.891006524188368f * ONE),
        (int) (0.882947592858927f * ONE),
        (int) (0.874619707139396f * ONE),
        (int) (0.866025403784439f * ONE),
        (int) (0.857167300702112f * ONE),
        (int) (0.848048096156426f * ONE),
        (int) (0.838670567945424f * ONE),
        (int) (0.829037572555042f * ONE),
        (int) (0.819152044288992f * ONE),
        (int) (0.809016994374947f * ONE),
        (int) (0.798635510047293f * ONE),
        (int) (0.788010753606722f * ONE),
        (int) (0.777145961456971f * ONE),
        (int) (0.766044443118978f * ONE),
        (int) (0.754709580222772f * ONE),
        (int) (0.743144825477394f * ONE),
        (int) (0.731353701619171f * ONE),
        (int) (0.719339800338651f * ONE),
        (int) (0.707106781186548f * ONE),
        (int) (0.694658370458997f * ONE),
        (int) (0.681998360062499f * ONE),
        (int) (0.669130606358858f * ONE),
        (int) (0.656059028990507f * ONE),
        (int) (0.642787609686539f * ONE),
        (int) (0.629320391049838f * ONE),
        (int) (0.615661475325658f * ONE),
        (int) (0.601815023152048f * ONE),
        (int) (0.587785252292473f * ONE),
        (int) (0.573576436351046f * ONE),
        (int) (0.559192903470747f * ONE),
        (int) (0.544639035015027f * ONE),
        (int) (0.529919264233205f * ONE),
        (int) (0.515038074910054f * ONE),
        (int) (0.5f * ONE),
        (int) (0.484809620246337f * ONE),
        (int) (0.469471562785891f * ONE),
        (int) (0.453990499739547f * ONE),
        (int) (0.438371146789077f * ONE),
        (int) (0.422618261740699f * ONE),
        (int) (0.4067366430758f * ONE),
        (int) (0.390731128489274f * ONE),
        (int) (0.374606593415912f * ONE),
        (int) (0.3583679495453f * ONE),
        (int) (0.342020143325669f * ONE),
        (int) (0.325568154457157f * ONE),
        (int) (0.309016994374948f * ONE),
        (int) (0.292371704722737f * ONE),
        (int) (0.275637355817f * ONE),
        (int) (0.258819045102521f * ONE),
        (int) (0.241921895599668f * ONE),
        (int) (0.224951054343865f * ONE),
        (int) (0.207911690817759f * ONE),
        (int) (0.190808995376545f * ONE),
        (int) (0.17364817766693f * ONE),
        (int) (0.156434465040231f * ONE),
        (int) (0.139173100960066f * ONE),
        (int) (0.121869343405148f * ONE),
        (int) (0.104528463267654f * ONE),
        (int) (0.0871557427476586f * ONE),
        (int) (0.0697564737441255f * ONE),
        (int) (0.0523359562429438f * ONE),
        (int) (0.0348994967025007f * ONE),
        (int) (0.0174524064372834f * ONE),
        (int) (1.22514845490862E-16f * ONE),
        (int) (-0.0174524064372832f * ONE),
        (int) (-0.0348994967025009f * ONE),
        (int) (-0.0523359562429436f * ONE),
        (int) (-0.0697564737441248f * ONE),
        (int) (-0.0871557427476579f * ONE),
        (int) (-0.104528463267653f * ONE),
        (int) (-0.121869343405148f * ONE),
        (int) (-0.139173100960066f * ONE),
        (int) (-0.156434465040231f * ONE),
        (int) (-0.17364817766693f * ONE),
        (int) (-0.190808995376545f * ONE),
        (int) (-0.207911690817759f * ONE),
        (int) (-0.224951054343865f * ONE),
        (int) (-0.241921895599668f * ONE),
        (int) (-0.25881904510252f * ONE),
        (int) (-0.275637355816999f * ONE),
        (int) (-0.292371704722736f * ONE),
        (int) (-0.309016994374948f * ONE),
        (int) (-0.325568154457157f * ONE),
        (int) (-0.342020143325669f * ONE),
        (int) (-0.3583679495453f * ONE),
        (int) (-0.374606593415912f * ONE),
        (int) (-0.390731128489274f * ONE),
        (int) (-0.4067366430758f * ONE),
        (int) (-0.422618261740699f * ONE),
        (int) (-0.438371146789077f * ONE),
        (int) (-0.453990499739546f * ONE),
        (int) (-0.469471562785891f * ONE),
        (int) (-0.484809620246337f * ONE),
        (int) (-0.5f * ONE),
        (int) (-0.515038074910054f * ONE),
        (int) (-0.529919264233205f * ONE),
        (int) (-0.544639035015027f * ONE),
        (int) (-0.559192903470747f * ONE),
        (int) (-0.573576436351046f * ONE),
        (int) (-0.587785252292473f * ONE),
        (int) (-0.601815023152048f * ONE),
        (int) (-0.615661475325658f * ONE),
        (int) (-0.629320391049838f * ONE),
        (int) (-0.642787609686539f * ONE),
        (int) (-0.656059028990507f * ONE),
        (int) (-0.669130606358858f * ONE),
        (int) (-0.681998360062498f * ONE),
        (int) (-0.694658370458997f * ONE),
        (int) (-0.707106781186547f * ONE),
        (int) (-0.719339800338651f * ONE),
        (int) (-0.73135370161917f * ONE),
        (int) (-0.743144825477394f * ONE),
        (int) (-0.754709580222772f * ONE),
        (int) (-0.766044443118978f * ONE),
        (int) (-0.777145961456971f * ONE),
        (int) (-0.788010753606722f * ONE),
        (int) (-0.798635510047293f * ONE),
        (int) (-0.809016994374947f * ONE),
        (int) (-0.819152044288992f * ONE),
        (int) (-0.829037572555041f * ONE),
        (int) (-0.838670567945424f * ONE),
        (int) (-0.848048096156426f * ONE),
        (int) (-0.857167300702112f * ONE),
        (int) (-0.866025403784438f * ONE),
        (int) (-0.874619707139396f * ONE),
        (int) (-0.882947592858927f * ONE),
        (int) (-0.891006524188368f * ONE),
        (int) (-0.898794046299167f * ONE),
        (int) (-0.90630778703665f * ONE),
        (int) (-0.913545457642601f * ONE),
        (int) (-0.92050485345244f * ONE),
        (int) (-0.927183854566787f * ONE),
        (int) (-0.933580426497202f * ONE),
        (int) (-0.939692620785908f * ONE),
        (int) (-0.945518575599317f * ONE),
        (int) (-0.951056516295154f * ONE),
        (int) (-0.956304755963035f * ONE),
        (int) (-0.961261695938319f * ONE),
        (int) (-0.965925826289068f * ONE),
        (int) (-0.970295726275996f * ONE),
        (int) (-0.974370064785235f * ONE),
        (int) (-0.978147600733806f * ONE),
        (int) (-0.981627183447664f * ONE),
        (int) (-0.984807753012208f * ONE),
        (int) (-0.987688340595138f * ONE),
        (int) (-0.99026806874157f * ONE),
        (int) (-0.992546151641322f * ONE),
        (int) (-0.994521895368273f * ONE),
        (int) (-0.996194698091746f * ONE),
        (int) (-0.997564050259824f * ONE),
        (int) (-0.998629534754574f * ONE),
        (int) (-0.999390827019096f * ONE),
        (int) (-0.999847695156391f * ONE),
        (int) (-1f * ONE),
        (int) (-0.999847695156391f * ONE),
        (int) (-0.999390827019096f * ONE),
        (int) (-0.998629534754574f * ONE),
        (int) (-0.997564050259824f * ONE),
        (int) (-0.996194698091746f * ONE),
        (int) (-0.994521895368273f * ONE),
        (int) (-0.992546151641322f * ONE),
        (int) (-0.99026806874157f * ONE),
        (int) (-0.987688340595138f * ONE),
        (int) (-0.984807753012208f * ONE),
        (int) (-0.981627183447664f * ONE),
        (int) (-0.978147600733806f * ONE),
        (int) (-0.974370064785235f * ONE),
        (int) (-0.970295726275997f * ONE),
        (int) (-0.965925826289068f * ONE),
        (int) (-0.961261695938319f * ONE),
        (int) (-0.956304755963035f * ONE),
        (int) (-0.951056516295154f * ONE),
        (int) (-0.945518575599317f * ONE),
        (int) (-0.939692620785909f * ONE),
        (int) (-0.933580426497202f * ONE),
        (int) (-0.927183854566787f * ONE),
        (int) (-0.92050485345244f * ONE),
        (int) (-0.913545457642601f * ONE),
        (int) (-0.90630778703665f * ONE),
        (int) (-0.898794046299167f * ONE),
        (int) (-0.891006524188368f * ONE),
        (int) (-0.882947592858927f * ONE),
        (int) (-0.874619707139396f * ONE),
        (int) (-0.866025403784439f * ONE),
        (int) (-0.857167300702112f * ONE),
        (int) (-0.848048096156426f * ONE),
        (int) (-0.838670567945424f * ONE),
        (int) (-0.829037572555042f * ONE),
        (int) (-0.819152044288992f * ONE),
        (int) (-0.809016994374948f * ONE),
        (int) (-0.798635510047293f * ONE),
        (int) (-0.788010753606722f * ONE),
        (int) (-0.777145961456971f * ONE),
        (int) (-0.766044443118978f * ONE),
        (int) (-0.754709580222772f * ONE),
        (int) (-0.743144825477395f * ONE),
        (int) (-0.731353701619171f * ONE),
        (int) (-0.719339800338652f * ONE),
        (int) (-0.707106781186548f * ONE),
        (int) (-0.694658370458998f * ONE),
        (int) (-0.681998360062498f * ONE),
        (int) (-0.669130606358858f * ONE),
        (int) (-0.656059028990507f * ONE),
        (int) (-0.64278760968654f * ONE),
        (int) (-0.629320391049838f * ONE),
        (int) (-0.615661475325659f * ONE),
        (int) (-0.601815023152048f * ONE),
        (int) (-0.587785252292473f * ONE),
        (int) (-0.573576436351046f * ONE),
        (int) (-0.559192903470747f * ONE),
        (int) (-0.544639035015027f * ONE),
        (int) (-0.529919264233206f * ONE),
        (int) (-0.515038074910054f * ONE),
        (int) (-0.5f * ONE),
        (int) (-0.484809620246337f * ONE),
        (int) (-0.469471562785891f * ONE),
        (int) (-0.453990499739547f * ONE),
        (int) (-0.438371146789077f * ONE),
        (int) (-0.4226182617407f * ONE),
        (int) (-0.4067366430758f * ONE),
        (int) (-0.390731128489275f * ONE),
        (int) (-0.374606593415912f * ONE),
        (int) (-0.358367949545301f * ONE),
        (int) (-0.342020143325669f * ONE),
        (int) (-0.325568154457158f * ONE),
        (int) (-0.309016994374948f * ONE),
        (int) (-0.292371704722736f * ONE),
        (int) (-0.275637355817f * ONE),
        (int) (-0.258819045102521f * ONE),
        (int) (-0.241921895599668f * ONE),
        (int) (-0.224951054343865f * ONE),
        (int) (-0.20791169081776f * ONE),
        (int) (-0.190808995376545f * ONE),
        (int) (-0.173648177666931f * ONE),
        (int) (-0.156434465040231f * ONE),
        (int) (-0.139173100960066f * ONE),
        (int) (-0.121869343405148f * ONE),
        (int) (-0.104528463267653f * ONE),
        (int) (-0.0871557427476583f * ONE),
        (int) (-0.0697564737441248f * ONE),
        (int) (-0.0523359562429444f * ONE),
        (int) (-0.0348994967025008f * ONE),
        (int) (-0.0174524064372844f * ONE),
    };
    
    /** Table of CRCs of all 8-bit messages. */
    private static int crc_table[];
   
    /** Make the table for a fast CRC. */
    private static void make_crc_table() {
        crc_table = new int[256];
        long c;
        int n, k;
   
        for (n = 0; n < 256; n++) {
            c = n;
            for (k = 0; k < 8; k++) {
                if ((c & 1) != 0)
                    c = 0xedb88320L ^ (c >> 1);
                else
                    c = c >> 1;
            }
            crc_table[n] = (int) c;
        }
    }
   
    /** Update a running CRC with the bytes buf[0..len-1]--the CRC
     * should be initialized to all 1's, and the transmitted value
     * is the 1's complement of the final running CRC (see the
     * crc() routine below)). */
    private static int update_crc(long crc, byte[] buf, int offset, int len) {
        long c = crc;
        int n, end;
   
        if (crc_table == null)
            make_crc_table();
        
        for (n = offset, end = offset + len; n < end; n++) {
            c = (((long) crc_table[(int) ((c ^ buf[n]) & 0xff)]) & 0xffffffffL) ^ (c >> 8);
        }
        return (int) c;
    }
   
    /** Return the CRC of the bytes buf[0..len-1]. */
    public static int crc(byte[] buf, int offset, int len) {
        return update_crc(0xffffffffL, buf, offset, len) ^ 0xffffffff;
    }
    
    private static class TextInputForm extends TextBox implements CommandListener {
        private PMIDlet midlet;
        private Command cmdOk;
        private Command cmdCancel;
        private boolean cancelled;
        
        public TextInputForm(PMIDlet midlet, String title, String text, int max, int constraints) {
            super(title, text, max, constraints);
            this.midlet = midlet;
            cmdOk = new Command("OK", Command.OK, 1);
            cmdCancel = new Command("Cancel", Command.CANCEL, 2);
            addCommand(cmdOk);
            addCommand(cmdCancel);
            setCommandListener(this);
        }
        
        public String getString() {
            String result;
            if (cancelled) {
                result = null;
            } else {
                result = super.getString();
            }
            return result;
        }
                
        public void commandAction(Command c, Displayable d) {
            if (c == cmdCancel) {
                cancelled = true;
            }
            synchronized (midlet) {
                midlet.notifyAll();
            }
        }
    }
}
