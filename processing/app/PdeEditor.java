#ifdef EDITOR

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

 
// play, stop, open, save, courseware, print, beautify
// height of button panel is 35

public class PdeEditor extends Panel implements PdeEnvironment {
  static final String DEFAULT_PROGRAM = "// type program here\n";
  
  // otherwise, if the window is resized with the message label
  // set to blank, it's preferredSize() will be fukered
  static final String EMPTY = "                                                                                                                                                             ";
  PdeApplet app;

  PdeEditorButtons buttons;
  //PdeGraphics graphics;
  PdeRunner runner;

  Frame frame;
  Window fullScreenWindow;

  Label status;
  TextArea textarea;

  String lastDirectory;
  String lastFile;

  boolean playing;


  public PdeEditor(PdeApplet app, String program) {
    this.app = app;
    setLayout(new BorderLayout());

    Color bgColor = 
      PdeApplet.getColor("bg_color", new Color(51, 102, 153));
    Color bgStippleColor = 
      PdeApplet.getColor("bg_stipple_color", null);
    Color tickColor = 
      PdeApplet.getColor("tick_color", new Color(204, 204, 204));
    Color gutterBgColor =
      PdeApplet.getColor("gutter_bg_color", new Color(0, 51, 102));
    Color buttonBgColor = 
      PdeApplet.getColor("button_bg_color", new Color(153, 153, 153));
    Color statusBgColor = 
      PdeApplet.getColor("status_bg_color", new Color(204, 204, 204));

    //int gwidth = PdeApplet.getInteger("graphics_width", 101);
    //int gheight = PdeApplet.getInteger("graphics_height", 101);

    //add("North", new PdeEditorLicensePlate());

    //Panel left = new Panel();
    //left.setLayout(new BorderLayout());

    Panel top = new Panel();
    top.setLayout(new BorderLayout());

    boolean privileges = PdeApplet.hasFullPrivileges();
    boolean courseware = PdeApplet.get("save_as") != null;
    buttons = new PdeEditorButtons(this, privileges, courseware, 
				   (privileges & !courseware), true);
    buttons.setBackground(buttonBgColor);
    //add("North", buttons);
    top.add("North", buttons);

    /*
    graphics = new PdeGraphics(gwidth, gheight, bgColor);
#ifndef OPENGL
    graphics = new PdeEditorGraphics(gwidth, gheight, tickColor,
				     bgColor, bgStippleColor, this);
#else
    if (PdeApplet.getBoolean("graphics_3D", false)) {
      graphics = new PdeEditorGraphics3D(gwidth, gheight, tickColor,
					 bgColor, bgStippleColor, this);
    } else {
      graphics = new PdeEditorGraphics(gwidth, gheight, tickColor,
				       bgColor, bgStippleColor, this);
    }
#endif
    */

    //left.add("Center", graphics);

    //left.setBackground(gutterBgColor);

    //Panel right = new Panel();
    //right.setLayout(new BorderLayout());

    Panel statusPanel = new Panel();
    statusPanel.setBackground(statusBgColor);
    statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    statusPanel.add(status = new Label(EMPTY));
    //right.add("North", statusPanel);
    top.add("South", statusPanel);

    add("North", top);

    if (program == null) program = DEFAULT_PROGRAM;
    textarea = new TextArea(program, 20, 48);
    textarea.setFont(PdeApplet.getFont("editor"));
    //right.add("Center", textarea);
    add("Center", textarea);

    //#ifdef FANCY
    //    right.add("South", PdeFancy.makeDescription());
    //#endif

    //this.add("West", left);
    //this.add("Center", right);

    if (!PdeApplet.isMacintosh()) {  // this still relevant?
      PdeEditorListener listener = new PdeEditorListener();
      textarea.addKeyListener(listener);
      textarea.addFocusListener(listener);
      textarea.addKeyListener(new PdeKeyListener(this));
    }

    runner = new PdeRunner(/*graphics,*/ this);
  }


  public void doPlay() {
    doStop();
    playing = true;
    buttons.play();

    runner.setProgram(textarea.getText());
    runner.start();

    // required so that key events go to the panel and <key> works
    //graphics.requestFocus();  // removed for pde
  }

#ifdef RECORDER
  public void doRecord() {
    doStop();
    PdeRecorder.start(this, graphics.width, graphics.height);
    doPlay();
  }
#endif

  public void doStop() {
#ifdef RECORDER
    if (!playing) return;
#endif
    terminate();
    buttons.clear();
    playing = false;
  }


  public void doOpen() {
    FileDialog fd = new FileDialog(new Frame(), 
				   "Open a PDE program...", 
				   FileDialog.LOAD);
    fd.setDirectory(lastDirectory);
    fd.setFile(lastFile);
    fd.show();
	
    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (filename == null) {
      buttons.clear();
      return; // user cancelled
    }
    File file = new File(directory, filename);

    try {
      FileInputStream input = new FileInputStream(file);
      int length = (int) file.length();
      byte data[] = new byte[length];
	    
      int count = 0;
      while (count != length) {
	data[count++] = (byte) input.read();
      }
      // set the last dir and file, so that they're
      // the defaults when you try to save again
      lastDirectory = directory;
      lastFile = filename;
	
      // once read all the bytes, convert it to the proper
      // local encoding for this system.
      //textarea.setText(app.languageEncode(data));
      if (app.encoding == null)
	textarea.setText(new String(data));
      else 
	textarea.setText(new String(data, app.encoding));

    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
	
    } catch (IOException e2) {
      e2.printStackTrace();
    }
    buttons.clear();
  }


  public void doSave() {
    message("Saving file...");
    String s = textarea.getText();
    FileDialog fd = new FileDialog(new Frame(), 
				   "Save PDE program as...", 
				   FileDialog.SAVE);
    fd.setDirectory(lastDirectory);
    fd.setFile(lastFile);
    fd.show();
	
    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (filename == null) {
      message(EMPTY);
      buttons.clear();
      return; // user cancelled
    }
    File file = new File(directory, filename);

    try {
      FileWriter writer = new FileWriter(file);
      writer.write(s);
      writer.flush();
      writer.close();

      lastDirectory = directory;
      lastFile = filename;
      message("Done saving file.");

    } catch (IOException e) {
      e.printStackTrace();
      message("Did not write file.");
    }
    buttons.clear();
  }


  public void doSnapshot() {
  /*
    //dbcp.msg("Sending your file to the server...");
    message("Sending your file to the server...");

    try {
      String programStr = textarea.getText();
      //byte imageData[] = dbrp.runners[dbrp.current].dbg.getPixels();
      //byte imageData[] = graphics.getPixels();
      String imageStr = new String(graphics.makeTiffData());

      URL appletUrl = app.getDocumentBase();
      String document = appletUrl.getFile();
      document = document.substring(0, document.lastIndexOf("?"));
      URL url = new URL("http", appletUrl.getHost(), document);

      URLConnection conn = url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setUseCaches(false);
      conn.setRequestProperty("Content-Type", 
			      "application/x-www-form-urlencoded");

      DataOutputStream printout = 
	new DataOutputStream(conn.getOutputStream());

      String content = 
	"save_as=" + URLEncoder.encode(PdeApplet.get("save_as")) + 
	"&save_image=" + URLEncoder.encode(imageStr) +
	"&save_program=" + URLEncoder.encode(programStr);

      printout.writeBytes(content);
      printout.flush();
      printout.close();
	    
      // what did they say back?
      DataInputStream input = 
	new DataInputStream(conn.getInputStream());
      String str = null;
      while ((str = input.readLine()) != null) {
	//System.out.println(str);
      }
      input.close();	    
      message("Done saving file.");

    } catch (Exception e) {
      e.printStackTrace();
      message("Problem: Your work could not be saved.");
    }
    buttons.clear();
  */
  }


  static byte tiffHeader[] = {
    77, 77, 0, 42, 0, 0, 0, 8, 0, 9, 0, -2, 0, 4, 0, 0, 0, 1, 0, 0,
    0, 0, 1, 0, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 3, 0, 0, 0, 1, 
    0, 0, 0, 0, 1, 2, 0, 3, 0, 0, 0, 3, 0, 0, 0, 122, 1, 6, 0, 3, 0, 
    0, 0, 1, 0, 2, 0, 0, 1, 17, 0, 4, 0, 0, 0, 1, 0, 0, 3, 0, 1, 21, 
    0, 3, 0, 0, 0, 1, 0, 3, 0, 0, 1, 22, 0, 3, 0, 0, 0, 1, 0, 0, 0, 0, 
    1, 23, 0, 4, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 8, 0, 8
  };

  static byte[] makeTiffData(int pixels[], int width, int height) {
    byte tiff[] = new byte[768 + width*height*3];
    System.arraycopy(tiffHeader, 0, tiff, 0, tiffHeader.length);
    tiff[30] = (byte) ((width >> 8) & 0xff);
    tiff[31] = (byte) ((width) & 0xff);
    tiff[42] = tiff[102] = (byte) ((height >> 8) & 0xff);
    tiff[43] = tiff[103] = (byte) ((height) & 0xff);
    int count = width*height*3;
    tiff[114] = (byte) ((count >> 24) & 0xff);
    tiff[115] = (byte) ((count >> 16) & 0xff);
    tiff[116] = (byte) ((count >> 8) & 0xff);
    tiff[117] = (byte) ((count) & 0xff);
    int index = 768;
    for (int i = 0; i < pixels.length; i++) {
      tiff[index++] = (byte) ((pixels[i] >> 16) & 0xff);
      tiff[index++] = (byte) ((pixels[i] >> 8) & 0xff);
      tiff[index++] = (byte) ((pixels[i] >> 0) & 0xff);
    }
    return tiff;
  }

  //public byte[] makeTiffData() {
  //return makeTiffData(pixels, width, height);
  //}

  public void doSaveTiff() {
  /*
    message("Saving TIFF image...");
    String s = textarea.getText();
    FileDialog fd = new FileDialog(new Frame(), 
				   "Save image as...", 
				   FileDialog.SAVE);
    fd.setDirectory(lastDirectory);
    fd.setFile("untitled.tif");
    fd.show();
	
    String directory = fd.getDirectory();
    String filename = fd.getFile();
    if (filename == null) return;

    File file = new File(directory, filename);
    try {
      FileOutputStream fos = new FileOutputStream(file);
      byte data[] = graphics.makeTiffData();
      fos.write(data);
      fos.flush();
      fos.close();

      lastDirectory = directory;
      message("Done saving image.");

    } catch (IOException e) {
      e.printStackTrace();
      message("An error occurred, no image could be written.");
    }
  */
  }


  /*
  static void writeTiffHeader(OutputStream os, int width, int height) 
    throws IOException {
    byte header[] = new byte[768];
    System.arraycopy(tiffHeader, 0, header, 0, tiffHeader.length);
    header[30] = (byte) ((width >> 8) & 0xff);
    header[31] = (byte) ((width) & 0xff);
    header[42] = header[102] = (byte) ((height >> 8) & 0xff);
    header[43] = header[103] = (byte) ((height) & 0xff);
    int count = width*height*3;
    header[114] = (byte) ((count >> 24) & 0xff);
    header[115] = (byte) ((count >> 16) & 0xff);
    header[116] = (byte) ((count >> 8) & 0xff);
    header[117] = (byte) ((count) & 0xff);
    os.write(header);
  }

  static void writeTiff(OutputStream os, int width, int height, int pixels[]) {
    writeTiffHeader(os, width, height);
    for (int i = 0; i < pixels.length; i++) {
      write((pixels[i] >> 16) & 0xff);
      write((pixels[i] >> 8) & 0xff);
      write(pixels[i] & 0xff);
    }
    os.flush();
  } 
  */

  /*
  static public byte[] makePgmData(byte inData[], int width, int height) {
    String headerStr = "P5 " + width + " " + height + " 255\n";
    byte header[] = headerStr.getBytes();
    int count = width * height;
    byte outData[] = new byte[header.length + count];
    System.arraycopy(header, 0, outData, 0, header.length);
    System.arraycopy(inData, 0, outData, header.length, count);
    return outData;
  }
  */

  public void doPrint() {
    /*
    Frame frame = new Frame(); // bullocks
    int screenWidth = getToolkit().getScreenSize().width;
    frame.reshape(screenWidth + 20, 100, screenWidth + 100, 200);
    frame.show();

    Properties props = new Properties();
    PrintJob pj = getToolkit().getPrintJob(frame, "PDE", props);
    if (pj != null) {
      Graphics g = pj.getGraphics();
      // awful way to do printing, but sometimes brute force is
      // just the way. java printing across multiple platforms is
      // outrageously inconsistent.
      int offsetX = 100;
      int offsetY = 100;
      int index = 0;
      for (int y = 0; y < graphics.height; y++) {
	for (int x = 0; x < graphics.width; x++) {
	  g.setColor(new Color(graphics.pixels[index++]));
	  g.drawLine(offsetX + x, offsetY + y,
		     offsetX + x, offsetY + y);
	}
      }
      g.dispose();
      g = null;
      pj.end();
    }
    frame.dispose();
    buttons.clear();
    */
  }


  public void doBeautify() {
    String prog = textarea.getText();
    if ((prog.charAt(0) == '#') || (prog.charAt(0) == ';')) {
      message("Only DBN code can be made beautiful.");
      buttons.clear();
      return;
    }
    char program[] = prog.toCharArray();
    StringBuffer buffer = new StringBuffer();
    boolean gotBlankLine = false;
    int index = 0;
    int level = 0;
	
    while (index != program.length) {
      int begin = index;
      while ((program[index] != '\n') &&
	     (program[index] != '\r')) {
	index++;
	if (program.length == index)
	  break;
      }
      int end = index;
      if (index != program.length) {
	if ((index+1 != program.length) &&
	    // treat \r\n from windows as one line
	    (program[index] == '\r') && 
	    (program[index+1] == '\n')) {
	  index += 2;
	} else {
	  index++;
	}		
      } // otherwise don't increment

      String line = new String(program, begin, end-begin);
      line = line.trim();
	    
      if (line.length() == 0) {
	if (!gotBlankLine) {
	  // let first blank line through
	  buffer.append('\n');
	  gotBlankLine = true;
	}
      } else {
	if (line.charAt(0) == '}') {
	  level--;
	}
	for (int i = 0; i < level*3; i++) {
	  buffer.append(' ');
	}
	buffer.append(line);
	buffer.append('\n');
	if (line.charAt(0) == '{') {
	  level++;
	}
	gotBlankLine = false;
      }
    }
    textarea.setText(buffer.toString());
    buttons.clear();
  }


  public void enableFullScreen() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    fullScreenWindow = new Window(new Frame());
    fullScreenWindow.setBounds(0, 0, screen.width, screen.height);
    fullScreenWindow.setBackground(new Color(102, 102, 102));
    fullScreenWindow.show();

    // not sure what to do with applet..
    // (since i can't bring the browser window to the front)
    // unless there's a method in AppletContext
    if (frame != null) frame.toFront();

    try {
      ((KjcEngine)(runner.engine)).window.toFront();
    } catch (Exception e) {
      // rather than writing code to check all the posible
      // errors with the above statement, just fail quietly
      //System.out.println("couldn't bring kjc engine window forward");
    }
    //if (runner.engine != null) {
    //if (runner.engine instanceof KjcEngine) {	
    //}
    //}

    buttons.clear();
  }

  public void disableFullScreen() {
    fullScreenWindow.hide();
    buttons.clear();
  }


  public void terminate() {   // part of PdeEnvironment
    runner.stop();
    message(EMPTY);
  }


  // TODO iron out bugs with this code under
  //      different platforms, especially macintosh
  public void highlightLine(int lnum) {
    if (lnum < 0) {
      textarea.select(0, 0);
      return;
    }
    //System.out.println(lnum);
    String s = textarea.getText();
    int len = s.length();
    //int lnum = .line;
    int st = -1, end = -1;
    int lc = 0;
    if (lnum == 0) st = 0;
    for (int i = 0; i < len; i++) {
      //if ((s.charAt(i) == '\n') || (s.charAt(i) == '\r')) {
      boolean newline = false;
      if (s.charAt(i) == '\r') {
	if ((i != len-1) && (s.charAt(i+1) == '\n')) i++;
	lc++;
	newline = true;
      } else if (s.charAt(i) == '\n') {
	lc++;
	newline = true;
      }
      if (newline) {
	if (lc == lnum)
	  st = i+1;
	else if (lc == lnum+1) {
	  end = i;
	  break;
	}
      }
    }
    if (end == -1) end = len;
    //System.out.println("st/end: "+st+"/"+end);
    textarea.select(st, end+1);
    //if (iexplorerp) {
    //textarea.invalidate();
    //textarea.repaint();
    //}
  }


  public void error(PdeException e) {   // part of PdeEnvironment
    if (e.line >= 0) highlightLine(e.line); 
    //dbcp.repaint(); // button should go back to 'play'
    //System.err.println(e.getMessage());
    message("Problem: " + e.getMessage());
    buttons.clearPlay();
    //showStatus(e.getMessage());
  }


  public void finished() {  // part of PdeEnvironment
#ifdef RECORDER
    PdeRecorder.stop();
#endif
    playing = false;
    buttons.clearPlay();
    message("Done.");
  }


  public void message(String msg) {  // part of PdeEnvironment
    status.setText(msg);
  }
  
  
  public void messageClear(String msg) {
    if (status.getText().equals(msg)) status.setText(EMPTY);
  }
}

#endif

