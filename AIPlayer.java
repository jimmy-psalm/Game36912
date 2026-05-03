import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AIPlayer - Difficult AI
 * Updated to match new rules: scoring counts ANY occupied cells (regardless of color)
 * Strategies: score priority, block opponent, extend occupied lines, random fallback
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
     * 
     * Under new rules: scoring counts ANY occupied cells (any color).
     * So "scoring potential" means: if AI places here, how many consecutive
     * occupied cells (of any color) will there be?
     */
    private int evaluateCell(int row, int col) {
        int score = 0;

        // Strategy 1: Can AI create a scoring line by placing here? (Highest priority)
        // Count consecutive OCCUPIED cells (any color) after placing
        score += evaluateScorePotential(row, col) * 100;

        // Strategy 2: Can AI block opponent from scoring?
        // Same logic - if opponent places here, how many consecutive occupied cells?
        // But we evaluate from AI's perspective: blocking means preventing opponent
        // from reaching 3/6/9/12. We simulate opponent placing here.
        score += evaluateBlockPotential(row, col) * 50;

        // Strategy 3: Extend existing occupied lines (any color)
        score += evaluateExtensionPotential(row, col) * 25;

        // Strategy 4: Center preference (slightly better strategic position)
        int centerDist = Math.abs(row - GameBoard.SIZE / 2) + Math.abs(col - GameBoard.SIZE / 2);
        score += (GameBoard.SIZE - centerDist);

        return score;
    }

    /**
     * Evaluate if placing here would create a scoring line
     * Counts consecutive OCCUPIED cells (any color) - this is the new rule
     */
    private int evaluateScorePotential(int row, int col) {
        // Temporarily place the piece (as AI)
        board.placePiece(row, col, GameBoard.AI);

        int score = 0;

        // Check horizontal - count ANY occupied cells
        int hCount = countConsecutiveOccupied(row, col, true);
        if (hCount >= 12) score += 12;
        else if (hCount >= 9) score += 9;
        else if (hCount >= 6) score += 6;
        else if (hCount >= 3) score += 3;

        // Check vertical - count ANY occupied cells
        int vCount = countConsecutiveOccupied(row, col, false);
        if (vCount >= 12) score += 12;
        else if (vCount >= 9) score += 9;
        else if (vCount >= 6) score += 6;
        else if (vCount >= 3) score += 3;

        // Remove the temporary piece
        removePiece(row, col);

        return score;
    }

    /**
     * Evaluate if opponent could score by placing here (blocking potential)
     */
    private int evaluateBlockPotential(int row, int col) {
        // Temporarily place as if HUMAN placed here
        board.placePiece(row, col, GameBoard.HUMAN);

        int score = 0;

        // Check horizontal
        int hCount = countConsecutiveOccupied(row, col, true);
        if (hCount >= 12) score += 12;
        else if (hCount >= 9) score += 9;
        else if (hCount >= 6) score += 6;
        else if (hCount >= 3) score += 3;

        // Check vertical
        int vCount = countConsecutiveOccupied(row, col, false);
        if (vCount >= 12) score += 12;
        else if (vCount >= 9) score += 9;
        else if (vCount >= 6) score += 6;
        else if (vCount >= 3) score += 3;

        // Remove the temporary piece
        removePiece(row, col);

        return score;
    }

    /**
     * Evaluate if placing here extends an existing occupied line
     */
    private int evaluateExtensionPotential(int row, int col) {
        // Temporarily place the piece (as AI)
        board.placePiece(row, col, GameBoard.AI);

        int score = 0;

        // Check horizontal extension - count ANY occupied cells
        int hCount = countConsecutiveOccupied(row, col, true);
        if (hCount >= 2 && hCount < 3) score += 1; // Close to 3
        else if (hCount >= 3 && hCount < 6) score += 2; // Already 3, extending toward 6
        else if (hCount >= 6 && hCount < 9) score += 3; // Already 6, extending toward 9
        else if (hCount >= 9 && hCount < 12) score += 4; // Already 9, extending toward 12

        // Check vertical extension
        int vCount = countConsecutiveOccupied(row, col, false);
        if (vCount >= 2 && vCount < 3) score += 1;
        else if (vCount >= 3 && vCount < 6) score += 2;
        else if (vCount >= 6 && vCount < 9) score += 3;
        else if (vCount >= 9 && vCount < 12) score += 4;

        // Remove the temporary piece
        removePiece(row, col);

        return score;
    }

    /**
     * Count consecutive OCCUPIED cells (any player) through (row, col) in a direction
     * Updated to match new rules: count ANY non-empty cell, regardless of player
     */
    private int countConsecutiveOccupied(int row, int col, boolean horizontal) {
        int count = 1;

        if (horizontal) {
            for (int c = col - 1; c >= 0; c--) {
                if (board.getCell(row, c) != GameBoard.EMPTY) count++;
                else break;
            }
            for (int c = col + 1; c < GameBoard.SIZE; c++) {
                if (board.getCell(row, c) != GameBoard.EMPTY) count++;
                else break;
            }
        } else {
            for (int r = row - 1; r >= 0; r--) {
                if (board.getCell(r, col) != GameBoard.EMPTY) count++;
                else break;
            }
            for (int r = row + 1; r < GameBoard.SIZE; r++) {
                if (board.getCell(r, col) != GameBoard.EMPTY) count++;
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
