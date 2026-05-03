import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AIPlayer - Difficult AI
 * Strategies: score priority, block opponent, extend own lines, random fallback
 */
public class AIPlayer {
    private GameBoard board;
    private Random random;

    public AIPlayer(GameBoard board) {
        this.board = board;
        this.random = new Random();
    }

    /**
     * Find the best move for the AI
     * Returns int[2] = {row, col}
     */
    public int[] getBestMove() {
        List<int[]> emptyCells = getAllEmptyCells();
        if (emptyCells.isEmpty()) return null;

        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();

        for (int[] cell : emptyCells) {
            int score = evaluateCell(cell[0], cell[1]);
            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(cell);
            } else if (score == bestScore) {
                bestMoves.add(cell);
            }
        }

        // Pick randomly among best moves
        return bestMoves.get(random.nextInt(bestMoves.size()));
    }

    /**
     * Evaluate a cell's value for the AI
     * Higher score = better move
     */
    private int evaluateCell(int row, int col) {
        int score = 0;

        // Strategy 1: Can AI score by placing here? (Highest priority)
        score += evaluateScorePotential(row, col, GameBoard.AI) * 100;

        // Strategy 2: Can AI block opponent's score? (High priority)
        score += evaluateScorePotential(row, col, GameBoard.HUMAN) * 50;

        // Strategy 3: Extend AI's own existing lines
        score += evaluateExtensionPotential(row, col, GameBoard.AI) * 25;

        // Strategy 4: Center preference (slightly better strategic position)
        int centerDist = Math.abs(row - GameBoard.SIZE / 2) + Math.abs(col - GameBoard.SIZE / 2);
        score += (GameBoard.SIZE - centerDist);

        return score;
    }

    /**
     * Evaluate if placing here would create a scoring line for the given player
     */
    private int evaluateScorePotential(int row, int col, int player) {
        // Temporarily place the piece
        board.placePiece(row, col, player);

        int score = 0;

        // Check horizontal
        int hCount = countConsecutive(row, col, player, true);
        if (hCount == 12) score += 12;
        else if (hCount == 9) score += 9;
        else if (hCount == 6) score += 6;
        else if (hCount == 3) score += 3;

        // Check vertical
        int vCount = countConsecutive(row, col, player, false);
        if (vCount == 12) score += 12;
        else if (vCount == 9) score += 9;
        else if (vCount == 6) score += 6;
        else if (vCount == 3) score += 3;

        // Remove the temporary piece
        removePiece(row, col);

        return score;
    }

    /**
     * Evaluate if placing here extends an existing line of the given player
     */
    private int evaluateExtensionPotential(int row, int col, int player) {
        int score = 0;

        // Temporarily place the piece
        board.placePiece(row, col, player);

        // Check horizontal extension
        int hCount = countConsecutive(row, col, player, true);
        if (hCount >= 2 && hCount < 3) score += 1; // Close to 3
        else if (hCount >= 3 && hCount < 6) score += 2; // Already 3, extending toward 6
        else if (hCount >= 6 && hCount < 9) score += 3; // Already 6, extending toward 9
        else if (hCount >= 9 && hCount < 12) score += 4; // Already 9, extending toward 12

        // Check vertical extension
        int vCount = countConsecutive(row, col, player, false);
        if (vCount >= 2 && vCount < 3) score += 1;
        else if (vCount >= 3 && vCount < 6) score += 2;
        else if (vCount >= 6 && vCount < 9) score += 3;
        else if (vCount >= 9 && vCount < 12) score += 4;

        // Remove the temporary piece
        removePiece(row, col);

        return score;
    }

    /**
     * Count consecutive pieces through (row, col) - same logic as GameEngine
     */
    private int countConsecutive(int row, int col, int player, boolean horizontal) {
        int count = 1;

        if (horizontal) {
            for (int c = col - 1; c >= 0; c--) {
                if (board.getCell(row, c) == player) count++;
                else break;
            }
            for (int c = col + 1; c < GameBoard.SIZE; c++) {
                if (board.getCell(row, c) == player) count++;
                else break;
            }
        } else {
            for (int r = row - 1; r >= 0; r--) {
                if (board.getCell(r, col) == player) count++;
                else break;
            }
            for (int r = row + 1; r < GameBoard.SIZE; r++) {
                if (board.getCell(r, col) == player) count++;
                else break;
            }
        }

        return count;
    }

    /**
     * Remove a piece from the board (used for temporary evaluation)
     */
    private void removePiece(int row, int col) {
        board.removePiece(row, col);
    }


    private List<int[]> getAllEmptyCells() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                if (board.isEmpty(r, c)) {
                    empty.add(new int[]{r, c});
                }
            }
        }
        return empty;
    }
}
