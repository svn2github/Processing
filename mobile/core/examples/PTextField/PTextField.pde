PScrollBar scrollbar;
PContainer screen;

void setup() {
  //// put a scrollbar on the right side of the screen
  scrollbar = new PScrollBar();
  scrollbar.setBounds(width - 4, 0, 4, height);
  
  //// let the container fill the rest of the screen
  screen = new PContainer();
  screen.scrolling = true;
  screen.scrollbar = scrollbar;
  screen.setBounds(0, 0, width - 4, height);
  
  int y = 0;
  
  PLabel label = new PLabel("Nickname:");
  label.calculateBounds(4, y, width - 12, PContainer.HEIGHT_UNBOUNDED);
  screen.add(label);  
  y = label.y + label.height + 4;
  
  PTextField textfield = new PTextField("");
  textfield.calculateBounds(4, y, width - 12, PContainer.HEIGHT_UNBOUNDED);
  screen.add(textfield);  
  y = textfield.y + textfield.height + 8;
    
  label = new PLabel("Password:");
  label.calculateBounds(4, y, width - 12, PContainer.HEIGHT_UNBOUNDED);
  screen.add(label);  
  y = label.y + label.height + 4;
  
  textfield = new PTextField("");
  textfield.password = true;
  textfield.calculateBounds(4, y, width - 12, PContainer.HEIGHT_UNBOUNDED);
  screen.add(textfield);  
  
  //// initialize the container (which initializes
  //// all of its children and the scrollbar)
  screen.initialize();
  screen.acceptFocus();
}

void draw() {
  //// draw the container (which draws its children)
  screen.draw();
}

void keyPressed() {
  //// let the container handle the input, it will
  //// pass it down to the focused child
  screen.keyPressed();
}

void keyReleased() {
  //// let the container handle the input, it will
  //// pass it down to the focused child
  screen.keyReleased();
}
