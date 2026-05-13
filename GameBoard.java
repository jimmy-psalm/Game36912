import java.util.ArrayList;
import java.util.List;

/**
 * GameBoard - 12x12 board data structure
 * Stores piece positions and red line segments
 */
public class GameBoard {
    public static final int SIZE = 12;
    public static final int EMPTY = 0;
    public static final int HUMAN = 1;
    public static final int AI = 2;

    private int[][] grid;
    private List<RedLine> redLines;
    private List<String> moveHistory;
    private int moveNumber;

    public GameBoard() {
        grid = new int[SIZE][SIZE];
        redLines = new ArrayList<>();
        moveHistory = new ArrayList<>();
        moveNumber = 0;
    }

    /**
     * Record a move in the history
     */
    public void recordMove(int row, int col, int player) {
        moveNumber++;
        String playerChar = (player == HUMAN) ? "h" : "a";
        String entry = padNumber(moveNumber, 3) + playerChar + "(" + row + "," + col + ")";
        moveHistory.add(entry);
    }

    private String padNumber(int num, int len) {
        StringBuilder sb = new StringBuilder();
        String s = String.valueOf(num);
        while (sb.length() + s.length() < len) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * Get move history as a list of entries
     */
    public List<String> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    /**
     * Get move history as a comma-separated string
     */
    public String getMoveHistoryString() {
        return String.join(", ", moveHistory);
    }

    /**
     * Clear move history
     */
    public void clearMoveHistory() {
        moveHistory.clear();
        moveNumber = 0;
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    public boolean isEmpty(int row, int col) {
        return grid[row][col] == EMPTY;
    }

    public boolean placePiece(int row, int col, int player) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) return false;
        if (grid[row][col] != EMPTY) return false;
        grid[row][col] = player;
        return true;
    }

    public void removePiece(int row, int col) {
        if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
            grid[row][col] = EMPTY;
        }
    }

    public boolean isFull() {

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == EMPTY) return false;
            }
        }
        return true;
    }

    public void addRedLine(RedLine line) {
        // Remove any existing red line on the same row/col that overlaps
        redLines.removeIf(existing -> existing.overlapsWith(line));
        redLines.add(line);
    }

    public List<RedLine> getRedLines() {
        return new ArrayList<>(redLines);
    }

    public void clearRedLines() {
        redLines.clear();
    }

    public void reset() {
        grid = new int[SIZE][SIZE];
        redLines.clear();
        moveHistory.clear();
        moveNumber = 0;
    }

    /**
     * RedLine represents a scored line segment with a colored line drawn through it
     * owner field determines the color: HUMAN = coral red, AI = teal
     */
    public static class RedLine {
        public final int row;
        public final int col;
        public final boolean horizontal; // true = horizontal, false = vertical
        public final int length;
        public final int owner; // HUMAN or AI - determines line color

        public RedLine(int row, int col, boolean horizontal, int length) {
            this(row, col, horizontal, length, HUMAN); // default owner
        }

        public RedLine(int row, int col, boolean horizontal, int length, int owner) {
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
            this.length = length;
            this.owner = owner;
        }

        /**
         * Check if this red line overlaps with another (same row/col, same orientation)
         */
        public boolean overlapsWith(RedLine other) {
            if (this.horizontal != other.horizontal) return false;
            if (this.horizontal) {
                if (this.row != other.row) return false;
                int thisStart = this.col;
                int thisEnd = this.col + this.length - 1;
                int otherStart = other.col;
                int otherEnd = other.col + other.length - 1;
                return !(thisEnd < otherStart || thisStart > otherEnd);
            } else {
                if (this.col != other.col) return false;
                int thisStart = this.row;
                int thisEnd = this.row + this.length - 1;
                int otherStart = other.row;
                int otherEnd = other.row + other.length - 1;
                return !(thisEnd < otherStart || thisStart > otherEnd);
            }
        }

        @Override
        public String toString() {
            return (horizontal ? "H" : "V") + "(" + row + "," + col + ") len=" + length + " owner=" + (owner == HUMAN ? "H" : "A");
        }
    }
}
