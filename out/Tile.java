import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Tile {
  public int xCoordinate;
  public int yCoordinate;
  private char symbol;
  private boolean flipped;
  // private final length = 
  public char getSymbol() {
    return symbol;
  }
  public boolean isFlipped() {
    return flipped;
  }
  public void setFlipped(boolean flipped) {
    this.flipped = flipped;
}
  public Tile(){
    this.flipped = false;
  }

}
