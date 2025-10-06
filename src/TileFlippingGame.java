
/*
 * @author  Idwel
 * @purpose Grade11 Semester2 end java GUI program
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

public class TileFlippingGame extends JFrame {
  private int rows;
  private int cols;
  final private int size = 800;
  private int identicalTilesToCancel;
  private TileButton[][] tiles;
  private final List<TileButton> selectedTiles;

  public TileFlippingGame() {
    this.selectedTiles = new ArrayList<>();
  }

  /*
   * arranging the number of rows and columns based on the input of different
   * *Tiles and identical Tiles To Cancel*.
   * total tiles = tile sets * identical tiles to cancel = rows * cols
   */
  public void initializeGame(int differentTiles, int identicalTilesToCancel) {
    this.identicalTilesToCancel = identicalTilesToCancel;
    int totalTiles = differentTiles * identicalTilesToCancel;
    calculateRowsAndCols(totalTiles);

    this.tiles = new TileButton[rows][cols];
    initializeGUI(differentTiles);
  }

  /*
   * algorithm: min(abs(rows=cols)), which means to make the rectangle as close to
   * a square as possible
   */
  private void calculateRowsAndCols(int totalTiles) {
    int sqrt = (int) Math.sqrt(totalTiles);
    int minDiff = Integer.MAX_VALUE;

    for (int r = sqrt; r > 0; r--) {
      if (totalTiles % r == 0) {
        int c = totalTiles / r;
        if (Math.abs(r - c) < minDiff) {
          minDiff = Math.abs(r - c);
          rows = r;
          cols = c;
        }
      }
    }
  }

  private void initializeGUI(int differentTiles) {
    /* use a rectangular grid to place all tiles */
    setTitle("Tile Flipping Game");
    setSize(size, size); // size of the game grid
    setLayout(new GridLayout(rows, cols));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    List<String> symbols = generateSymbols(differentTiles); // all symbols need for a game
    Collections.shuffle(symbols); // randomize tile alphabet order.

    int index = 0;
    /* Place the buttons on the grid one by one */
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        tiles[i][j] = new TileButton(symbols.get(index));
        tiles[i][j].addActionListener(new TileButtonListener()); // make the tile clickable
        add(tiles[i][j]);
        index++;
      }
    }

    setVisible(true);
  }

  /*
   * order of symbol generation: A-Z, then AA-ZZ, then AAA-ZZZ...
   * each symbol can be represented by a single integer
   */
  private List<String> generateSymbols(int differentTiles) {
    List<String> symbols = new ArrayList<>();
    for (int i = 0; i < differentTiles; i++) {
      String symbol = getSymbolForIndex(i);
      for (int j = 0; j < identicalTilesToCancel; j++) {
        symbols.add(symbol);
      }
    }
    return symbols;
  }

  private String getSymbolForIndex(int index) {
    StringBuilder symbol = new StringBuilder();
    while (index >= 0) {
      symbol.insert(0, (char) ('A' + index % 26));
      index = index / 26 - 1;
    }
    return symbol.toString();
  }

  /*
   * class of single tile unit
   * the symbol is placed at the center of the tile body
   */
  private class TileButton extends JButton {
    private boolean isFlipped;
    private final String symbol;

    public TileButton(String symbol) {
      this.symbol = symbol;
      this.isFlipped = false;
      setText("");
      int fontsize = (int) Math.round(size / ( 2 * Math.sqrt(rows * cols)));
      setFont(new Font("Arial", Font.BOLD, fontsize)); // Set the default font size and style
    }

    public void flip() {
      isFlipped = !isFlipped;
      setText(isFlipped ? symbol : "");
    }

    public String getSymbol() {
      return symbol;
    }

    public boolean isFlipped() {
      return isFlipped;
    }
  }

  /*
   * I searched it online. Its an actionlistener.
   * 
   * @reference:
   * https://stackoverflow.com/questions/21879243/how-to-create-on-click-event-for
   * -buttons-in-swing
   */
  private class TileButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) { // when the tile getts clicked
      TileButton clickedTile = (TileButton) e.getSource(); // the tile get clicked
      if (!clickedTile.isFlipped() && selectedTiles.size() < identicalTilesToCancel) {
        clickedTile.flip();
        selectedTiles.add(clickedTile);

        if (selectedTiles.size() == identicalTilesToCancel) {
          /*
           * when the number of total clicked tiles equal to the identicle tile set, check
           * for its matching
           */
          Timer timer = new Timer(500, (ActionEvent evt) -> {
              checkForMatch();
          });
          timer.setRepeats(false);
          timer.start();
        }
      }
    }
  }


  private void checkForMatch() {
    String firstSymbol = selectedTiles.get(0).getSymbol(); // use the symbol on the first clicked tile as a tester
    boolean allMatch = true;

    for (TileButton tile : selectedTiles) {
      if (!tile.getSymbol().equals(firstSymbol)) {
        /* if not all symbols are equal, then fail to cancel them out. */
        allMatch = false;
        break;
      }
    }

    if (allMatch) {
      for (TileButton tile : selectedTiles) {
        tile.setEnabled(false); // freeze the canceled tiles
      }
    } else {
      /* flip the tiles back if they're not matched */
      for (TileButton tile : selectedTiles) {
        tile.flip();
      }
    }
    selectedTiles.clear();
  }

  /*
   * gets the settings for game difficulty
   */
  public void showDifficultySettings() {
    while (true) {
      JPanel panel = new JPanel(new GridLayout(2, 2));
      // get the two variables needed to start a game.
      JTextField differentTilesField = new JTextField();
      JTextField identicalField = new JTextField();

      panel.add(new JLabel("Number of Different Tiles:"));
      panel.add(differentTilesField);
      panel.add(new JLabel("Identical tiles to cancel:"));
      panel.add(identicalField);

      /*
       * JOptionPane is a easy way to pop up notification messages
       * 
       * @reference: https://stackoverflow.com/questions/7080205/popup-message-boxes
       */
      int result = JOptionPane.showConfirmDialog(null, panel, "Enter Game Settings", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        // exception handling
        try {
          int differentTiles = Integer.parseInt(differentTilesField.getText());
          int identical = Integer.parseInt(identicalField.getText());

          if (differentTiles <= 0 || identical <= 0) {
            throw new NumberFormatException("Values must be positive integers.");
          }

          if (differentTiles * identical >= 2000) {
            throw new Exception("number too large");
          }

          JOptionPane.showMessageDialog(this, "Good luck and have fun!", "Enjoy the Game",
              JOptionPane.INFORMATION_MESSAGE);
          initializeGame(differentTiles, identical);
          break;
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(null, "Please enter valid POSITIVE INTEGER values for all variables.",
              "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
          JOptionPane.showMessageDialog(null, "You definitely cannot handle this many tiles.",
              "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        /* giving an option to exit the game */
        System.exit(0);
      }
    }
  }

  public void showIntroduction() {
    /**
     * showing introductions before the game
     */
    String[] introductions = {
        """
        Welcome to the Tile Flipping Game!
        
        Instructions:
        1. Click on a tile to flip it.
        2. Match a number of identical tiles in a row to cancel them out.
        3. If the tiles do not match, they will be flipped back.
        4. Cancel all tiles to win the game.
        
        """, 
        """
        This game is quite different from similar games
        where you need flip only two tiles with the same pattern to cancel them. 
        
        You can now assign the number of identical tiles required to cancel them out,
        and the according number of identical tiles will be generated automatically.""", 
        """
        You will then be asked for the number of total groups of tiles
        and the number of tiles in each group.

        Please fill in POSITIVE NUMBERS only.

        You will receive warnings if you do not do so."""};
      for (String introduction : introductions) {
          JOptionPane.showMessageDialog(this, introduction, "Introductions", JOptionPane.INFORMATION_MESSAGE);
      }
  }

  /*
   * Starts the game
   */
  public static void gameStart() {
    TileFlippingGame game = new TileFlippingGame();
    game.showIntroduction();
    game.showDifficultySettings();
  }

  public static void main(String[] args) {
    // show all text in english
    Locale.setDefault(Locale.ENGLISH);
    gameStart();
  }
}
