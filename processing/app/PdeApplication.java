import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;


public class PdeApplication extends PdeApplet 
#ifdef RECORDER
implements ActionListener
#endif
{
  Frame frame;
  WindowAdapter windowListener;

  static public void main(String args[]) {
    PdeApplication app = new PdeApplication();
    //if (args.length != 0) {
    //app.setProgramFile(args[0]);
    //}

    app.frame.show();
  }

  public PdeApplication() {
    frame = new Frame(" p r o c e s s i n g ");

    windowListener = new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	System.exit(0);
      }
    };
    frame.addWindowListener(windowListener);

#ifdef RECORDER
    MenuBar menubar = new MenuBar();
    Menu goodies = new Menu("Processing");
    goodies.add(new MenuItem("Save QuickTime movie..."));
    goodies.add(new MenuItem("Quit"));
    goodies.addActionListener(this);
    menubar.add(goodies);
    frame.setMenuBar(menubar);
#endif

    properties = new Properties();
    try {
      properties.load(new FileInputStream("lib/pde.properties"));

    } catch (Exception e) {
      System.err.println("Error reading pde.properties");
      e.printStackTrace();
      System.exit(1);
    }
    int width = getInteger("width", 600);
    int height = getInteger("height", 350);
    // ms jdk requires that BorderLayout is set explicitly
    frame.setLayout(new BorderLayout());
    frame.add("Center", this);
    init();
    Insets insets = frame.getInsets();
    frame.reshape(50, 50, width + insets.left + insets.right, 
		  height + insets.top + insets.bottom);

    // i don't like this being here, but..
    //((PdeEditor)environment).graphics.frame = frame;
    ((PdeEditor)environment).frame = frame;

    frame.pack();
    //frame.show();
  }

#ifdef RECORDER
  public void actionPerformed(ActionEvent event) {
    String command = event.getActionCommand();
    if (command.equals("Save QuickTime movie...")) {
      ((PdeEditor)environment).doRecord();
    } else if (command.equals("Quit")) {
      System.exit(0);
    }
  }
#endif
}

#endif
