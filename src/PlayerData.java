import java.io.*;
import java.util.List;
import java.util.ArrayList;
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
