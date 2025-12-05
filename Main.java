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

// Method for the model
// Defines the tile class to be used in the game
class Tile {
    private char symbol;
     // NEW: Variable to track if a card has been revealed before (Required for point system)
    private boolean flipped;
    private boolean seen;
    // NEW: Variable to track if card has been matched 
    private boolean matched;

    public Tile(char symbol) {
        this.symbol = symbol;
        this.flipped = false;
        // NEW: Initialize seen as false
        this.seen = false; 
        this.matched = false;
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
        tiles.clear(); // ensure fresh start
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
        // reset flipsRemaining for next turn
        flipsRemaining = 2;
    }

    // Flips the tile when controller calls it
    public void flipTile(Tile tile) {
        tile.setFlipped(true);
    }

    public void restartGame() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        tiles.clear();
        matchesFound = 0;
        flipsRemaining = 2;
        playerScore = 0;
        timeFinished = false;  // Restarts timeFinished flag
        secondsElapsed = 0;
        initializeTiles(6); // Change the number of pairs as per your preference
        startTimer(); // Starts timer for a new game
    }

    public void startTimer() {
        // ensure any existing timer is canceled first
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();
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
    public List<Tile> getTiles() { return tiles; }

    // Method to help the controller update the model of how many matches
    public void updateMatches() { matchesFound = matchesFound + 1; }

    // Method to help the controller update the model of how many flips
    public void updateFlips() { flipsRemaining = flipsRemaining - 1; }

    // Adds a negative value to the score
    public void negativeScore() { playerScore = playerScore - 2; }

    // Adds a positive value to the score
    public void positiveScore() { playerScore = playerScore + 10; }

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

    // Helper in case controller wants to increment seconds 
    public void incrementSeconds() { 
        secondsElapsed++;
        if (secondsElapsed >= gameDuration) {
            timeFinished = true;
        }
    }
}

// Terminal output view
class GameOutput {
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

    public void show(String message) {
        System.out.println(message);
    }
}

// The second view in this program
class GameGUI extends JFrame {
    private final MemoryGame memory;
    private final java.util.List<JButton> buttons = new ArrayList<>();
    
    public JLabel scoreLabel;
    public JLabel timeLabel;
    public JLabel messageLabel;
    public javax.swing.Timer swingTimer;

    public GameGUI(MemoryGame memory, GameController controller) {
        this.memory = memory;
        
        // Setting the size, layout, and title
        setTitle("Memory Game - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Makes the top. Displays the score, seconds, and game duration
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoreLabel = new JLabel("Score: " + memory.getScore());
        timeLabel = new JLabel("Time: " + memory.getSeconds() + " / " + memory.getGameDuration());
        top.add(scoreLabel);
        top.add(Box.createHorizontalStrut(20));
        top.add(timeLabel);
        add(top, BorderLayout.NORTH);

        // Makes the grid for the cards
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
                    controller.tileTurn(index);
                }
            });
            // initial enabled state depends on whether tile is matched
            Tile t = memory.getTiles().get(i);
            btn.setEnabled(!t.isMatched());
            buttons.add(btn);
            grid.add(btn);
        }
        add(grid, BorderLayout.CENTER);

        // The bottom of the JPanel. Allows restarting
        JPanel bottomPanel = new JPanel(new BorderLayout());
        messageLabel = new JLabel("Find all the pairs!", SwingConstants.CENTER);
        bottomPanel.add(messageLabel, BorderLayout.CENTER);

        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> controller.restartGame());
        bottomPanel.add(restartButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refreshBoard() {
        for (int i = 0; i < memory.getTiles().size(); i++) {
            Tile tile = memory.getTileFromTiles(i);
            JButton btn = buttons.get(i);

            if (tile.isFlipped()) {
                btn.setText(String.valueOf(tile.getSymbol()));
            } else {
                btn.setText("?");
            }
            // disable button if tile matched otherwise enable it
            btn.setEnabled(!tile.isMatched());
        }
    }

    public java.util.List<JButton> getButtons() {
        return buttons;
    }
}

// The method that acts as the controller
// Makes the model and view connect
class GameController {
    private final MemoryGame memory;
    private GameOutput output; // Instance of the view class
    private GameGUI gui; // Instance of the second view class
    private int firstIndex = -1;
    private int secondIndex = -1;
    private boolean processingTurn = false;
    private Scanner scanner;

    public GameController(MemoryGame memory) {
        this.memory = memory;
        this.output = new GameOutput();
        this.scanner = new Scanner(System.in);
    }
    
    // If the user chooses the GUI option this executes
    public void playGUI() {
        SwingUtilities.invokeLater(() -> {
            memory.initializeTiles(6); 
            gui = new GameGUI(memory, this);
            gui.refreshBoard();            // ensure buttons reflect tiles at start
            gui.setVisible(true);
            memory.startTimer();           // start the model timer
            startTimer();                  // start GUI swing timer 
        });
    }

    // Handles the tile flipping in the GUI
    public void tileTurn(int index) {
        if (processingTurn || memory.timeIsFinished()) return;
        
        Tile tile = memory.getTileFromTiles(index);
        if (tile.isFlipped()) {
            gui.messageLabel.setText("Tile already flipped.");
            return;
        }
        
        memory.flipTile(tile);
        memory.updateFlips();
        gui.getButtons().get(index).setText(String.valueOf(tile.getSymbol()));
        
        if (firstIndex == -1) {
            firstIndex = index;
        } else if (secondIndex == -1) {
            secondIndex = index;
            processingTurn = true;
            evaluateMatch();
        }
    }

    // Starts the timer in the GUI
    private void startTimer() {
        gui.swingTimer = new javax.swing.Timer(500, e -> {
            gui.timeLabel.setText("Time: " + memory.getSeconds() + " / " + memory.getGameDuration());
            gui.scoreLabel.setText("Score: " + memory.getScore());
            if (memory.timeIsFinished()) {
                gui.messageLabel.setText("Time's up! Game over.");
                disableButtons();
                gui.swingTimer.stop();
            }
        });
        gui.swingTimer.start();
    }

    // Evaluates the matches in the GUI after two tiles
    private void evaluateMatch() {
        Tile t1 = memory.getTileFromTiles(firstIndex);
        Tile t2 = memory.getTileFromTiles(secondIndex);

        // Handles if the tiles match
        if (t1.getSymbol() == t2.getSymbol() && !t1.isMatched() && !t2.isMatched()) {
            gui.messageLabel.setText("Match found! (+10 Points)"); // Calls the GUI for the output
            memory.updateMatches(); // Calls the model to update the matches and score
            memory.positiveScore();
            t1.setMatched(true);
            t2.setMatched(true);
            t1.setSeen(true);
            t2.setSeen(true);
            gui.getButtons().get(firstIndex).setEnabled(false);
            gui.getButtons().get(secondIndex).setEnabled(false);
            resetSelection();
            
            // Handles if user finds all the matches
            if (memory.getMatches() == memory.getTiles().size() / 2) {
                gui.messageLabel.setText("You found all the matches!");
                memory.endGame();
                disableButtons();
                if (gui.swingTimer != null) gui.swingTimer.stop();
                JOptionPane.showMessageDialog(gui, "Congratulations!\nFinal score: " + memory.getScore() 
                    + "\nTime: " + memory.getSeconds() + " seconds");
            } else {
                processingTurn = false;
            }
        } else { // Handles if the tiles do not match
            java.util.List<Tile> currentTurnTiles = Arrays.asList(t1, t2);

            boolean penalty = false;
            for (Tile t : currentTurnTiles) {
                if (t.isSeen()) penalty = true;
            }

            if (penalty) {
                gui.messageLabel.setText("No match. You saw this card before! (-2 Points)");
                memory.negativeScore();
            } else {
                gui.messageLabel.setText("No match. Next turn.");
            }

            for (Tile t : currentTurnTiles) t.setSeen(true);

            javax.swing.Timer flipBackTimer = new javax.swing.Timer(700, e -> {
                memory.resetFlippedTiles();
                gui.refreshBoard();
                resetSelection();
                processingTurn = false;
            });
            flipBackTimer.setRepeats(false);
            flipBackTimer.start();
        }

        gui.scoreLabel.setText("Score: " + memory.getScore());
    }

    private void resetSelection() {
        firstIndex = -1;
        secondIndex = -1;
    }

    private void disableButtons() {
        for (JButton btn : gui.getButtons()) {
            btn.setEnabled(false);
        }
    }

    // Restarts game by resetting variables. Allows user to know by messages
    public void restartGame() {
        if (gui.swingTimer != null && gui.swingTimer.isRunning()) {
            gui.swingTimer.stop();
        }
        processingTurn = false;
        firstIndex = -1;
        secondIndex = -1;
        memory.restartGame();
        gui.refreshBoard();
        gui.scoreLabel.setText("Score: " + memory.getScore());
        gui.messageLabel.setText("New game");
        startTimer();
    }
    
    // Playing method if the user selects the terminal view
    public void play() {
        memory.initializeTiles(6);
        memory.startTimer();

        while (memory.getMatches() < memory.getTiles().size() / 2) {
            if (memory.timeIsFinished()) {
                output.show("Time's up! Game over.");
                memory.endGame();
                return;
            }
            
            // Calls the terminal to display the board and messages
            output.displayBoard(memory.getTiles());
            output.show("Enter the tile number to flip, 'q' to quit, or 'r' to restart: ");
            String input = userInput(); // Gets user input from the view
            
            // If time is up the game is cancelled
            if (memory.timeIsFinished()) {
                output.show("Time's up! Game over.");
                memory.endGame();
                return;
            }

            // Handles user input if they quit or restart
            if (input.equalsIgnoreCase("q")) {
                output.show("Quitting the game. Goodbye!");
                memory.endGame();
                memory.cancelTimer();
                return;
            } else if (input.equalsIgnoreCase("r")) {  
                output.show("Restarting the game...");
                memory.restartGame();
                continue;
            }
            // Handles user errors
            int tileIndex;
            try {
                tileIndex = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                output.show("Invalid input. Please enter a tile number.");
                continue;
            }
            
            if (tileIndex < 0 || tileIndex >= memory.getTiles().size()) {
                output.show("Invalid tile number. Please enter a valid tile number.");
                continue;
            }
            // Checks a specific tile
            Tile tile = memory.getTileFromTiles(tileIndex);
            if (tile.isFlipped()) {
                output.show("Tile already flipped. Try again.");
            } else {
                memory.flipTile(tile);
                memory.updateFlips();
                // If a match is found, matchesFound and playerScore increases
                if (memory.checkForMatch(tile)) {
                    output.show("Match found! (+10 Points)");
                    memory.updateMatches();
                    memory.positiveScore(); 
                    // Loops through tiles to mark matched ones as "seen"
                    for (Tile t : memory.getTiles()) {
                        if (t.isFlipped()) {
                            t.setSeen(true);
                        }
                    }
                } else if (memory.getFlips() == 0) {
                    // Check for penalties before resetting tiles
                    // Identify the cards involved in this turn
                    List<Tile> currentTurnTiles = new ArrayList<>();
                    for (Tile t : memory.getTiles()) {
                        if (t.isFlipped()) {
                            currentTurnTiles.add(t);
                        }
                    }
                    //  Check if any of these cards were seen before
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
                        memory.negativeScore();
                    } else {
                        output.show("No match. Out of flips. Next turn.");
                    }

                    for (Tile t : currentTurnTiles) {
                        t.setSeen(true);
                    }

                    memory.resetFlippedTiles(); // Reset the flips remaining for the next turn
                } else {
                    output.show("No match. Try again.");
                }
            }
        }
        
        // Calls the view to output the user results
        output.show("Congratulations! You found all the matches.");
        output.show("Your final score: " + memory.getScore());
        output.show("Time elapsed: " + memory.getSeconds() + " seconds");
        memory.endGame();
        scanner.close();
    }

    public String userInput() {
        String input = scanner.nextLine();
        return input;
    }
}


public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        MemoryGame game = new MemoryGame();
        GameController controller = new GameController(game);
        
        // Handles user input. They can choose the view
        System.out.println("Select the game mode: T for terminal, G for GUI");
        String input = scan.next();
        
        if (input.equalsIgnoreCase("T")) {
            controller.play();
        } else if (input.equalsIgnoreCase("G")) {
            controller.playGUI();
        } else {
            System.out.println("Invalid input. Try again");
        }
        scan.close();
    }
}
