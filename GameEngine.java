import java.util.List;
import java.util.ArrayList;

/**
 * GameEngine - Core rules engine
 * Handles line detection, scoring, extra turns, game end
 * 
 * Rules:
 * - Colors are only for player designation, NOT for scoring
 * - Scoring counts consecutive OCCUPIED cells (any color) in a line
 * - If a player creates a line of 3/6/9/12 occupied cells, they score
 * - Scoring player gets an extra turn; keeps going until they don't score
 * - Then it's the other player's turn
 */
public class GameEngine {
    private GameBoard board;
    private int humanScore;
    private int aiScore;
    private int currentPlayer; // HUMAN or AI
    private boolean gameOver;
    private boolean extraTurn; // true if current player gets another turn

    // Valid scoring lengths
    private static final int[] VALID_LENGTHS = {3, 6, 9, 12};

    public GameEngine(GameBoard board) {
        this.board = board;
        this.humanScore = 0;
        this.aiScore = 0;
        this.currentPlayer = GameBoard.HUMAN; // Human goes first
        this.gameOver = false;
        this.extraTurn = false;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int getHumanScore() {
        return humanScore;
    }

    public int getAiScore() {
        return aiScore;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean hasExtraTurn() {
        return extraTurn;
    }

    public void setExtraTurn(boolean extraTurn) {
        this.extraTurn = extraTurn;
    }

    /**
     * Process a move: place piece, check for scores, manage turns
     * Returns a MoveResult describing what happened
     * 
     * Scoring: counts consecutive OCCUPIED cells (any color, any player)
     * in horizontal or vertical direction through the placed piece.
     */
    public MoveResult processMove(int row, int col, int player) {
        MoveResult result = new MoveResult();

        // Place the piece
        if (!board.placePiece(row, col, player)) {
            result.valid = false;
            return result;
        }
        result.valid = true;

        // Check for scored lines - count ANY occupied cells (regardless of color)
        List<ScoredLine> scoredLines = checkAllLines(row, col);

        if (!scoredLines.isEmpty()) {
            // Find the highest scoring line
            ScoredLine bestLine = scoredLines.get(0);
            for (ScoredLine line : scoredLines) {
                if (line.score > bestLine.score) {
                    bestLine = line;
                }
            }

            // Add score to the player who placed the piece
            if (player == GameBoard.HUMAN) {
                humanScore += bestLine.score;
            } else {
                aiScore += bestLine.score;
            }

            // Add red line
            board.addRedLine(new GameBoard.RedLine(
                bestLine.startRow, bestLine.startCol,
                bestLine.horizontal, bestLine.length
            ));

            result.scored = true;
            result.score = bestLine.score;
            result.redLine = new GameBoard.RedLine(
                bestLine.startRow, bestLine.startCol,
                bestLine.horizontal, bestLine.length
            );

            // Player gets an extra turn
            extraTurn = true;
        } else {
            extraTurn = false;
        }

        // Check if board is full
        if (board.isFull()) {
            gameOver = true;
            result.gameEnded = true;
        }

        return result;
    }

    /**
     * Switch to the other player
     */
    public void switchPlayer() {
        currentPlayer = (currentPlayer == GameBoard.HUMAN) ? GameBoard.AI : GameBoard.HUMAN;
    }

    /**
     * Check all horizontal and vertical lines passing through (row, col)
     * Counts ANY occupied cells (regardless of which player owns them)
     * Returns list of scored lines (lengths 3, 6, 9, 12)
     */
    private List<ScoredLine> checkAllLines(int row, int col) {
        List<ScoredLine> results = new ArrayList<>();

        // Check horizontal - count ANY occupied cells
        int hCount = countConsecutiveOccupied(row, col, true);
        for (int len : VALID_LENGTHS) {
            if (hCount >= len) {
                int startCol = findHorizontalStart(row, col);
                results.add(new ScoredLine(row, startCol, row, startCol + len - 1, true, len, len));
            }
        }

        // Check vertical - count ANY occupied cells
        int vCount = countConsecutiveOccupied(row, col, false);
        for (int len : VALID_LENGTHS) {
            if (vCount >= len) {
                int startRow = findVerticalStart(row, col);
                results.add(new ScoredLine(startRow, col, startRow + len - 1, col, false, len, len));
            }
        }

        return results;
    }

    /**
     * Count consecutive OCCUPIED cells (any player) through (row, col) in a direction
     * This is the key change: we count ANY non-empty cell, regardless of player
     */
    private int countConsecutiveOccupied(int row, int col, boolean horizontal) {
        int count = 1; // The cell itself is occupied

        if (horizontal) {
            // Count left
            for (int c = col - 1; c >= 0; c--) {
                if (board.getCell(row, c) != GameBoard.EMPTY) count++;
                else break;
            }
            // Count right
            for (int c = col + 1; c < GameBoard.SIZE; c++) {
                if (board.getCell(row, c) != GameBoard.EMPTY) count++;
                else break;
            }
        } else {
            // Count up
            for (int r = row - 1; r >= 0; r--) {
                if (board.getCell(r, col) != GameBoard.EMPTY) count++;
                else break;
            }
            // Count down
            for (int r = row + 1; r < GameBoard.SIZE; r++) {
                if (board.getCell(r, col) != GameBoard.EMPTY) count++;
                else break;
            }
        }

        return count;
    }

    /**
     * Find the start of a horizontal run of occupied cells
     */
    private int findHorizontalStart(int row, int col) {
        int start = col;
        for (int c = col - 1; c >= 0; c--) {
            if (board.getCell(row, c) != GameBoard.EMPTY) start = c;
            else break;
        }
        return start;
    }

    /**
     * Find the start of a vertical run of occupied cells
     */
    private int findVerticalStart(int row, int col) {
        int start = row;
        for (int r = row - 1; r >= 0; r--) {
            if (board.getCell(r, col) != GameBoard.EMPTY) start = r;
            else break;
        }
        return start;
    }

    /**
     * Get the winner: 0 = tie, 1 = human, 2 = AI
     */
    public int getWinner() {
        if (humanScore > aiScore) return GameBoard.HUMAN;
        if (aiScore > humanScore) return GameBoard.AI;
        return 0; // tie
    }

    /**
     * Reset the game
     */
    public void reset() {
        board.reset();
        humanScore = 0;
        aiScore = 0;
        currentPlayer = GameBoard.HUMAN;
        gameOver = false;
        extraTurn = false;
    }

    /**
     * Result of a move
     */
    public static class MoveResult {
        public boolean valid;
        public boolean scored;
        public int score;
        public GameBoard.RedLine redLine;
        public boolean gameEnded;

        public MoveResult() {
            this.valid = false;
            this.scored = false;
            this.score = 0;
            this.redLine = null;
            this.gameEnded = false;
        }
    }

    /**
     * Represents a scored line
     */
    private static class ScoredLine {
        public int startRow, startCol;
        public int endRow, endCol;
        public boolean horizontal;
        public int length;
        public int score;

        public ScoredLine(int startRow, int startCol, int endRow, int endCol,
                          boolean horizontal, int length, int score) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.horizontal = horizontal;
            this.length = length;
            this.score = score;
        }
    }
}
