import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/* TO DO

GUI in the GameOutput class

Point system in the MemoryGame class

Use the GameController class to call the methods from the
point system and the GUI*/


// Method for the model
// Defines the tile class to be used in the game
class Tile {
    private char symbol;
    private boolean flipped;
    // NEW: Variable to track if a card has been revealed before (Required for point system)
    private boolean seen; 
    // NEW: Variable to track if card has been matched
    private boolean matched;

    public Tile(char symbol) {
        this.symbol = symbol;
        this.flipped = false;
        // NEW: Initialize seen as false
        this.seen = false; 
    }

    public char getSymbol() { return symbol; }

    public boolean isFlipped() { return flipped; }

    public void setFlipped(boolean flipped) { this.flipped = flipped; }

    // NEW: Getter for the seen status
    public boolean isSeen() { return seen; }

    // NEW: Setter for the seen status
    public void setSeen(boolean seen) { this.seen = seen; }

    // NEW: Getter for matched status
    public boolean isMatched() { return matched; }

    // NEW: Setter for matched status
    public void setMatched(boolean matched) { this.matched = matched; }
}

// Another method for the model
// Contains the core game logic
class MemoryGame {
    private List<Tile> tiles;
    private int matchesFound;
    private int flipsRemaining;
    private int playerScore;
    private Timer timer;
    private int secondsElapsed;
    private final int gameDuration;
    private boolean timeFinished = false;

    public MemoryGame() {
        tiles = new ArrayList<>();
        matchesFound = 0;
        flipsRemaining = 2; // Set the number of allowed flips per turn
        playerScore = 0;
        secondsElapsed = 0;
        gameDuration = 60; // Set the game duration in seconds
    }
    
    // Initializes tiles in the output
    public void initializeTiles(int pairs) {
        for (char symbol = 'A'; symbol < 'A' + pairs; symbol++) {
            tiles.add(new Tile(symbol));
            tiles.add(new Tile(symbol));
        }
        Collections.shuffle(tiles);
    }

    public boolean checkForMatch(Tile tile) {
        int count = 0;
        for (Tile t : tiles) {
            if (t.isFlipped() && t.getSymbol() == tile.getSymbol()) {
                count++;
            }
        }
        return count == 2;
    }

public void resetFlippedTiles() {
    for (Tile tile : tiles) {
        // only flip back tiles that are NOT permanently matched
        if (tile.isFlipped() && !tile.isMatched()) {
            tile.setFlipped(false);
        }
    }
}


    // Flips the tile when controller calls it
    public void flipTile(Tile tile) {
        tile.setFlipped(true);
    }

    public void restartGame() {
        // Makes sure old timers are stopped
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        tiles.clear();
        matchesFound = 0;
        flipsRemaining = 2;
        playerScore = 0;
        timeFinished = false; // Restarts timeFinished flag
        secondsElapsed = 0;
        initializeTiles(6); // Change the number of pairs as per your preference
        startTimer(); // Starts timer for a new game
    }

    public void startTimer() {
        timer = new Timer(); // Initializes a new timer
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsElapsed++;
                if (secondsElapsed >= gameDuration) {
                    timeFinished = true;
                    endGame();
                }
            }
        }, 1000, 1000);
    }

    // Gets a specific tile from the tiles list
    public Tile getTileFromTiles(int tileIndex) {
        return tiles.get(tileIndex);
    }

    // Ends the game by cancelling the timer
    public void endGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timeFinished = true;
    }

    public boolean timeIsFinished() {
        return timeFinished;
    }

    // Method to cancel the timer
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // Method to get tiles from the model
    public List<Tile> getTiles () { return tiles; }

    // Method to help the controller update the model of how many matches
    public void updateMatches() { matchesFound = matchesFound + 1; }

    // Method to help the controller update the model of how many flips
    public void updateFlips() { flipsRemaining = flipsRemaining - 1; }

    // Method to help the controller update the model of the current score
    // MODIFIED: Changed signature to accept specific point values (positive or negative)
    public void updateScore(int points) { playerScore = playerScore + points; }

    // Method to get the matches from the model
    public int getMatches() { return matchesFound; }

    // Method to get the seconds elapsed from the model
    public int getSeconds() { return secondsElapsed; }

    // Method to get the current flips from the model
    public int getFlips() { return flipsRemaining; }

    // Method to get the current score from the model
    public int getScore() { return playerScore; }

    // Method to get the game duration from the model
    public int getGameDuration() { return gameDuration; }
}

// The method that acts as the controller
// Makes the model and view connect
class GameController {
    GameOutput output = new GameOutput(); // Instance of the view class
    private final MemoryGame memory;
    Scanner scanner = new Scanner(System.in);
    // Makes the GameController object with data from MemoryGame (model) class
    public GameController(MemoryGame memory) {
        this.memory = memory;                  
    }
    
    public void play() {
        memory.startTimer();

        while (memory.getMatches() < memory.getTiles().size() / 2) {
            // If time is up, the game is cancelled
            if (memory.timeIsFinished()) {
                output.show("Time's up! Game over.");
                memory.endGame();
                return;
            }

            // Displays board. Calls the tiles from model class
            output.displayBoard(memory.getTiles());
            output.show("Enter the tile number to flip, 'q' to quit, or 'r' to restart: ");
            // Gets user input
            String input = userInput();
            // If time is up, the game is cancelled
            if (memory.timeIsFinished()) {
                output.show("Time's up! Game over.");
                memory.endGame();
                return;
            }

            if (input.equalsIgnoreCase("q")) {
                // Calls the view method to display message
                output.show("Quitting the game. Goodbye!");
                memory.endGame();
                memory.cancelTimer();
                return;
            // Allows user to restart the game
            } else if (input.equalsIgnoreCase("r")) {  
                // Calls the view method to display message
                output.show("Restarting the game...");
                memory.restartGame();
                continue;
            }
            int tileIndex;
            try {
                tileIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                // Calls the view method to display message
                output.show("Invalid input. Please enter a tile number.");
                continue;
            }
            if (tileIndex < 0 || tileIndex >= memory.getTiles().size()) {
                // Calls the view method to display message
                output.show("Invalid tile number. Please enter a valid tile number.");
                continue;
            }
           
            // Gets specific tile from the model
            Tile tile = memory.getTileFromTiles(tileIndex);
            if (tile.isFlipped()) {
                // Calls the view method to display message
                output.show("Tile already flipped. Try again.");
            } else {
                memory.flipTile(tile);
                memory.updateFlips();
                // If a match is found, matchesFound and playerScore increases
                if (memory.checkForMatch(tile)) {
                    output.show("Match found! (+10 Points)"); // NEW: Updated message
                    memory.updateMatches();
                    
                    // MODIFIED: Add 10 points for a match instead of 1
                    memory.updateScore(10); 
                    
                    // NEW: Loop through tiles to mark matched ones as "seen"
                    for (Tile t : memory.getTiles()) {
                        if (t.isFlipped()) {
                            t.setSeen(true);
                        }
                    }

                } else if (memory.getFlips() == 0) {
                    
                    // NEW: Logic to check for penalties before resetting tiles
                    // Identify the cards involved in this turn
                    List<Tile> currentTurnTiles = new ArrayList<>();
                    for (Tile t : memory.getTiles()) {
                        if (t.isFlipped()) {
                            currentTurnTiles.add(t);
                        }
                    }
                    
                    // NEW: Check if any of these cards were seen before
                    boolean penalty = false;
                    for (Tile t : currentTurnTiles) {
                        // If it's seen AND it's not the one we just flipped (unless we flipped the same one twice, which is prevented elsewhere)
                        // Actually simplified: If it's seen, we should have remembered it.
                        if (t.isSeen()) {
                            penalty = true;
                        }
                    }

                    if (penalty) {
                        output.show("No match. You saw this card before! (-2 Points). Next turn.");
                        memory.updateScore(-2); // NEW: Deduct points
                    } else {
                        output.show("No match. Out of flips. Next turn.");
                    }

                    // NEW: Mark these cards as seen now that they have been revealed
                    for (Tile t : currentTurnTiles) {
                        t.setSeen(true);
                    }

                    memory.resetFlippedTiles(); // Reset the flips remaining for the next turn
                } else {
                    output.show("No match. Try again.");
                }
            }
        }
        // Prints the scores for the users. Calls the model for data
            output.show("Congratulations! You found all the matches.");
            output.show("Your final score: " + memory.getScore());
            output.show("Time elapsed: " + memory.getSeconds() + " seconds");
            memory.endGame();
            scanner.close();
    }

    // Gets user input
    public String userInput() {
        String input = scanner.nextLine();
        return input;
    }
}

// The method that acts as a view
// GUI and terminal output
class GameOutput {
    // View portion. This is the written view
    public void displayBoard(List<Tile> tiles) {
        System.out.println("\n------- Memory Game -------");
        System.out.print("   ");
        for (int i = 0; i < tiles.size(); i++) {
            System.out.print(i + "\t");
        }
        System.out.println();
        for (int i = 0; i < tiles.size(); i++) {
            if (i % 6 == 0) {
                System.out.println();
                System.out.print(i / 6 + "  ");
            }
            Tile tile = tiles.get(i);
            if (tile.isFlipped()) {
                System.out.print(tile.getSymbol() + "\t");
            } else {
                System.out.print("[" + i + "]\t");
            }
        }
        System.out.println("\n---------------------------");
    }

    // For the controller method to print any message
    public void show(String message) {
        System.out.println(message);
    }
}

// GUI View
class GameGUI extends JFrame {

    private final MemoryGame memory;
    private final java.util.List<JButton> buttons = new ArrayList<>();

    private JLabel scoreLabel;
    private JLabel timeLabel;
    private JLabel messageLabel;

    private int firstIndex = -1;
    private int secondIndex = -1;
    private boolean processingTurn = false;

    private javax.swing.Timer swingTimer;

    public GameGUI(MemoryGame memory) {
        this.memory = memory;
        memory.startTimer();
        set();
        startTime();
    }

    private void set() {
        setTitle("Memory Game - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // score and time show on top panel
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoreLabel = new JLabel("Score: " + memory.getScore());
        timeLabel = new JLabel("Time: " + memory.getSeconds() + " / " + memory.getGameDuration());
        top.add(scoreLabel);
        top.add(Box.createHorizontalStrut(20));
        top.add(timeLabel);

        add(top, BorderLayout.NORTH);

        // cards grid
        JPanel grid = new JPanel();

        int totalTiles = memory.getTiles().size();
        int cols = 6; 
        int rows = (int) Math.ceil(totalTiles / (double) cols);

        grid.setLayout(new GridLayout(rows, cols, 10, 10));

        for (int i = 0; i < totalTiles; i++) {
            JButton btn = new JButton("?");
            final int index = i;

            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tileTurn(index);
                }
            });

            buttons.add(btn);
            grid.add(btn);
        }

        add(grid, BorderLayout.CENTER);

        // bottom panel of message and restart button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageLabel = new JLabel("Find all the pairs!", SwingConstants.CENTER);
        bottomPanel.add(messageLabel, BorderLayout.CENTER);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartGame());
        bottomPanel.add(restartButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startTime() {
        swingTimer = new javax.swing.Timer(500, e -> {
            timeLabel.setText("Time: " + memory.getSeconds() + " / " + memory.getGameDuration());
            scoreLabel.setText("Score: " + memory.getScore());
            if (memory.timeIsFinished()) {
                messageLabel.setText("Time's up! Game over.");
                disableButtons();
                swingTimer.stop();
            }
        });
        swingTimer.start();
    }

    private void tileTurn(int index) {
        if (processingTurn || memory.timeIsFinished()) return;
        Tile tile = memory.getTileFromTiles(index);
        if (tile.isFlipped()) {
            messageLabel.setText("Tile already flipped.");
            return;
        }
        memory.flipTile(tile);
        memory.updateFlips();
        buttons.get(index).setText(String.valueOf(tile.getSymbol()));
        if (firstIndex == -1) 
            firstIndex = index;
        else if (secondIndex == -1) {
            secondIndex = index;
            processingTurn = true;
            eval();
        }
    }

    private void eval() {
    Tile t1 = memory.getTileFromTiles(firstIndex);
    Tile t2 = memory.getTileFromTiles(secondIndex);

    if (t1.getSymbol() == t2.getSymbol() && !t1.isMatched() && !t2.isMatched()) {
        messageLabel.setText("Match found! (+10 Points)");
        memory.updateMatches();
        memory.updateScore(10);
        // mark them as matched
        t1.setMatched(true);
        t2.setMatched(true);
        // mark them as seen
        t1.setSeen(true);
        t2.setSeen(true);
        buttons.get(firstIndex).setEnabled(false);
        buttons.get(secondIndex).setEnabled(false);
        resetSelection();
        // for winning
        if (memory.getMatches() == memory.getTiles().size() / 2) {
            messageLabel.setText("You found all the matches!");
            memory.endGame();
            disableButtons();
            if (swingTimer != null) swingTimer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations!\nFinal score: " + memory.getScore() + "\nTime: " + memory.getSeconds() + " seconds");
        } else processingTurn = false;
     } else {
        java.util.List<Tile> currentTurnTiles = Arrays.asList(t1, t2);

        boolean penalty = false;
        for (Tile t : currentTurnTiles) {
            if (t.isSeen()) penalty = true;
        }

        if (penalty) {
            messageLabel.setText("No match. You saw this card before! (-2 Points)");
            memory.updateScore(-2);
        } else messageLabel.setText("No match. Next turn.");

        for (Tile t : currentTurnTiles) t.setSeen(true);

        javax.swing.Timer flipBackTimer = new javax.swing.Timer(700, e -> {
            memory.resetFlippedTiles();
            refreshBoard();
            resetSelection();
            processingTurn = false;
        });
        flipBackTimer.setRepeats(false);
        flipBackTimer.start();
    }

    scoreLabel.setText("Score: " + memory.getScore());
}

    private void refreshBoard() {
        for (int i = 0; i < memory.getTiles().size(); i++) {
            Tile tile = memory.getTileFromTiles(i);
            JButton btn = buttons.get(i);

            // if flipped then show letter otherwise show ?
            if (tile.isFlipped()) btn.setText(String.valueOf(tile.getSymbol()));  
            else {
                btn.setText("?");
                btn.setEnabled(true);
            }
        }
    }

    private void resetSelection() {
        firstIndex = -1;
        secondIndex = -1;
    }

    private void disableButtons() {
        for (JButton btn : buttons) btn.setEnabled(false);
    }

private void restartGame() {
    if (swingTimer != null && swingTimer.isRunning()) swingTimer.stop();

    processingTurn = false;
    firstIndex = -1;
    secondIndex = -1;
    memory.restartGame();
    refreshBoard();
    scoreLabel.setText("Score: " + memory.getScore());
    messageLabel.setText("New game");
    startTime();
}

}

public class Main {
    public static void main(String[] args) {
        // for console
        // MemoryGame game = new MemoryGame();
        // game.initializeTiles(6); 
        // GameController controller = new GameController(game);
        // controller.play();

        // for GUI
        SwingUtilities.invokeLater(() -> {
            MemoryGame game = new MemoryGame();
            game.initializeTiles(6); 
            GameGUI gui = new GameGUI(game);
            gui.setVisible(true);
        });
    }
}


