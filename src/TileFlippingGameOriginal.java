/*
 * @author  Idwel
 * @purpose Grade11 Semester2 end java GUI program
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class TileFlippingGameOriginal extends JFrame {
  private int rows;
  private int cols;
  private int identicalTilesToCancel;
  private TileButton[][] tiles;
  private List<TileButton> selectedTiles;
  private int score;
  /* to show the time and flipCount at the end of a game */
  private Timer flipBackTimer;
  private long startTime;
  private int flipCount;

  public TileFlippingGameOriginal() {
    this.selectedTiles = new ArrayList<>();
    this.score = 0;
    this.flipCount = 0; // Initialize flip counter
  }

  public void initializeGame(int differentTiles, int identicalTilesToCancel) {
    this.identicalTilesToCancel = identicalTilesToCancel;
    int totalTiles = differentTiles * identicalTilesToCancel;
    calculateRowsAndCols(totalTiles);

    this.tiles = new TileButton[rows][cols];

    initializeGUI(differentTiles);
    this.startTime = System.currentTimeMillis(); // Record the start time
    this.flipCount = 0; // Reset flip counter at the start of a new game
    this.score = 0; // Reset score at the start of a new game
  }

  /*
   * arranging the number of rows and columns based on the input of different
   * *Tiles and identical Tiles To Cancel*.
   * tile sets * identical tiles to cancel = rows * cols
   * alogrithm: min(abs(rows=cols))
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
    setTitle("Tile Flipping Game");
    setSize(800, 800); // size of the game grid
    setLayout(new GridLayout(rows, cols));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    List<String> symbols = generateSymbols(differentTiles);
    Collections.shuffle(symbols); // randomize tile alphabet order.

    int index = 0;
    /* Place the buttons on the grid one by one */
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        tiles[i][j] = new TileButton(i, j, symbols.get(index));
        tiles[i][j].addActionListener(new TileButtonListener());
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
    private int row;
    private int col;
    private boolean isFlipped;
    private String symbol;

    public TileButton(int row, int col, String symbol) {
      this.row = row;
      this.col = col;
      this.symbol = symbol;
      this.isFlipped = false;
      setText("");
    }

    /* let the symbols on the tiles change size according to the size of the tile */
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (isFlipped) {
        Font currentFont = g.getFont();
        int fontSize = Math.min(getWidth(), getHeight()) / 2;
        Font newFont = currentFont.deriveFont((float) fontSize);
        g.setFont(newFont);
        FontMetrics fm = g.getFontMetrics(newFont);
        int textWidth = fm.stringWidth(symbol);
        int textHeight = fm.getAscent();
        g.drawString(symbol, (getWidth() - textWidth) / 2, (getHeight() + textHeight) / 2 - fm.getDescent());
      }
    }

    public void flip() {
      isFlipped = !isFlipped;
      repaint();
    }

    public String getSymbol() {
      return symbol;
    }

    public boolean isFlipped() {
      return isFlipped;
    }
  }

  /*
   * actions after the tile is clicked
   */
  private class TileButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      TileButton clickedTile = (TileButton) e.getSource(); // the tile get clicked
      if (!clickedTile.isFlipped && selectedTiles.size() < identicalTilesToCancel) {
        clickedTile.flip();
        selectedTiles.add(clickedTile);
        flipCount++; // Increment the flip counter

        if (selectedTiles.size() == identicalTilesToCancel) {
          /*
           * actions when the number of flipped tiles equals to the size of identical
           * tiles set
           */
          flipBackTimer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              checkForMatch();
              flipBackTimer.stop();
            }
          });
          flipBackTimer.setRepeats(false);
          flipBackTimer.start();
        }
      }
    }
  }

  /*
   * showing statistic data at the end of the game.
   */
  private void checkForMatch() {
    String firstSymbol = selectedTiles.get(0).getSymbol();
    boolean allMatch = true;

    for (TileButton tile : selectedTiles) {
      if (!tile.getSymbol().equals(firstSymbol)) {
        allMatch = false;
        break;
      }
    }

    if (allMatch) {
      score++;
      for (TileButton tile : selectedTiles) {
        tile.setEnabled(false); // freeze the canceled tiles
      }
    } else {
      /* flip the tiles back if they're not matached */
      for (TileButton tile : selectedTiles) {
        tile.flip();
      }
    }
    selectedTiles.clear();

    if (score == (rows * cols) / identicalTilesToCancel) {
      long endTime = System.currentTimeMillis();
      long totalTime = endTime - startTime;
      PlayerData.saveScore(score);

      // Convert totalTime from milliseconds to seconds
      double totalTimeSeconds = totalTime / 1000.0;
      int totalTiles = rows * cols;
      double howManyTimesEachGetClicked = (double) flipCount / (totalTiles);
      int replay = JOptionPane.showConfirmDialog(this,
          String.format(
              "You have canceled all the tiles and thus finish this round!\nTime spent: %.3f seconds\nTotal flips: %d\nTotal tiles: %d\nIn average you clicked each tile %.2f times\n\nDo you want to play again?",
              totalTimeSeconds, flipCount, totalTiles, howManyTimesEachGetClicked),
          "Game Over",
          JOptionPane.YES_NO_OPTION);

      if (replay == JOptionPane.YES_OPTION) {
        resetGame(); // Reset game state
        showDifficultySettings();
      } else {
        System.exit(0);
      }
    }
  }

  /*
   * Reset the game state for replaying
   */
  private void resetGame() {
    getContentPane().removeAll(); // Remove all components from the frame
    getContentPane().revalidate();
    getContentPane().repaint();

    this.tiles = null;
    this.selectedTiles.clear();
    this.score = 0;
    this.flipCount = 0;
    this.flipBackTimer = null;
  }

  /*
   * gets the settings for game difficulty
   */
  public void showDifficultySettings() {
    while (true) {
      JPanel panel = new JPanel(new GridLayout(2, 2));
      JTextField differentTilesField = new JTextField();
      JTextField identicalField = new JTextField();

      panel.add(new JLabel("Number of Different Tiles:"));
      panel.add(differentTilesField);
      panel.add(new JLabel("Identical tiles to cancel:"));
      panel.add(identicalField);

      int result = JOptionPane.showConfirmDialog(null, panel, "Enter Game Settings", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        // exception handling
        try {
          int differentTiles = Integer.parseInt(differentTilesField.getText());
          int identical = Integer.parseInt(identicalField.getText());

          if (differentTiles <= 0 || identical <= 0) {
            throw new NumberFormatException("Values must be positive integers.");
          }

          if (differentTiles * identical >= 2000){
            throw new Exception("number too large");
          }
          JOptionPane.showMessageDialog(this, "Good luck and have fun!", "Enjoy the Game",
              JOptionPane.INFORMATION_MESSAGE);
          initializeGame(differentTiles, identical);
          break;
        } catch (NumberFormatException e) {
          JOptionPane.showMessageDialog(null, "Please enter valid POSITIVE INTEGER values for all variables.",
              "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e){
          JOptionPane.showMessageDialog(null, "You definitely cannot handle this much tiles.",
              "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        /* giving an option to exit the game */
        System.exit(0);
      }
    }
  }

  private void showIntroduction() {
    /**
     * showing introductions before the game
     */
    String[] introductions = {
        "Welcome to the Tile Flipping Game!\n\n"
            + "Instructions:\n"
            + "1. Click on a tile to flip it.\n"
            + "2. Match a number of identical tiles in a row to cancel them out.\n"
            + "3. If the tiles do not match, they will be flipped back.\n"
            + "4. Cancel all tiles to win the game.\n\n",
        "This game is quite different from similar games\n"
            + "where you need filp only two tiles with the same pattern to cancel them. \n\n"
            + "You can now assign the number of identical tiles required to cancel them out, \n"
            + "and the according number of identical tiles will be generated automatically.",
        "You will then be asked for the number of total groups of tiles\nand the number of tiles in each group."
            + "\n\nPlease fill in POSITIVE NUMBERS only."
            + "\n\nYou will receive warnings if you do not do so."
    };
    for (int i = 0; i < introductions.length; i++) {
      JOptionPane.showMessageDialog(this, introductions[i], "Introductions", JOptionPane.INFORMATION_MESSAGE);
    }
  }

  /*
   * Starts the game
   */
  public static void gameStart() {
    SwingUtilities.invokeLater(() -> {
      TileFlippingGameOriginal game = new TileFlippingGameOriginal();
      game.showIntroduction();
      game.showDifficultySettings();
    });
  }

  public static void main(String[] args) {
    gameStart();
  }
}

class PlayerData {
  private static final String DATA_FILE = "player_data.txt";

  public static void saveScore(int score) {
    try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_FILE, true)))) {
      out.println(score);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static List<Integer> loadScores() {
    List<Integer> scores = new ArrayList<>();
    try (BufferedReader in = new BufferedReader(new FileReader(DATA_FILE))) {
      String line;
      while ((line = in.readLine()) != null) {
        scores.add(Integer.parseInt(line));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return scores;
  }
}