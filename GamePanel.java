import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.List;

/**
 * GamePanel - Main Swing UI
 * Renders the board, handles mouse clicks, displays scores
 */
public class GamePanel extends JPanel {
    private GameBoard board;
    private GameEngine engine;
    private AIPlayer ai;

    // UI constants
    private static final int CELL_SIZE = 48;
    private static final int GRID_PADDING = 30;
    private static final int PIECE_RADIUS = 18;
    private static final Color BOARD_COLOR = new Color(220, 200, 170);
    private static final Color GRID_LINE_COLOR = new Color(100, 80, 60);
    private static final Color HUMAN_COLOR = new Color(50, 100, 220);
    private static final Color AI_COLOR = new Color(220, 120, 30);
    private static final Color RED_LINE_COLOR = new Color(220, 30, 30);
    private static final Color BG_COLOR = new Color(240, 230, 210);

    // UI components
    private JLabel statusLabel;
    private JLabel humanScoreLabel;
    private JLabel aiScoreLabel;
    private JButton resetButton;
    private JButton undoButton;
    private JFrame parentFrame;

    // AI thinking timer
    private Timer aiTimer;
    private boolean aiThinking;

    public GamePanel(JFrame frame) {
        this.parentFrame = frame;
        this.board = new GameBoard();
        this.engine = new GameEngine(board);
        this.ai = new AIPlayer(board);
        this.aiThinking = false;

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Top panel: title and status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("3-6-9-12 连线棋", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 40, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        statusLabel = new JLabel("人类先手 (蓝色) - 请点击棋盘下子", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        statusLabel.setForeground(new Color(80, 60, 40));
        topPanel.add(statusLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Center: board drawing panel
        BoardPanel boardPanel = new BoardPanel();
        boardPanel.setPreferredSize(new Dimension(
            GRID_PADDING * 2 + CELL_SIZE * GameBoard.SIZE,
            GRID_PADDING * 2 + CELL_SIZE * GameBoard.SIZE
        ));
        add(boardPanel, BorderLayout.CENTER);

        // Bottom panel: scores and reset
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(BG_COLOR);

        humanScoreLabel = new JLabel("人类: 0 分");
        humanScoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        humanScoreLabel.setForeground(HUMAN_COLOR);

        aiScoreLabel = new JLabel("AI: 0 分");
        aiScoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        aiScoreLabel.setForeground(AI_COLOR);

        resetButton = new JButton("重置游戏");
        resetButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        resetButton.setBackground(new Color(200, 180, 150));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());

        undoButton = new JButton("悔棋");
        undoButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        undoButton.setBackground(new Color(176, 128, 96));
        undoButton.setForeground(Color.WHITE);
        undoButton.setFocusPainted(false);
        undoButton.addActionListener(e -> undoMove());

        bottomPanel.add(humanScoreLabel);
        bottomPanel.add(aiScoreLabel);
        bottomPanel.add(resetButton);
        bottomPanel.add(undoButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Inner class for drawing the board
     */
    private class BoardPanel extends JPanel {
        public BoardPanel() {
            setBackground(BOARD_COLOR);
            setPreferredSize(new Dimension(
                GRID_PADDING * 2 + CELL_SIZE * GameBoard.SIZE,
                GRID_PADDING * 2 + CELL_SIZE * GameBoard.SIZE
            ));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleMouseClick(e.getX(), e.getY());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawBoard(g2d);
            drawPieces(g2d);
            drawRedLines(g2d);
        }

        private void drawBoard(Graphics2D g) {
            g.setColor(GRID_LINE_COLOR);
            g.setStroke(new BasicStroke(1.5f));

            // Draw grid lines
            for (int i = 0; i <= GameBoard.SIZE; i++) {
                // Horizontal lines
                int y = GRID_PADDING + i * CELL_SIZE;
                g.drawLine(GRID_PADDING, y, GRID_PADDING + GameBoard.SIZE * CELL_SIZE, y);

                // Vertical lines
                int x = GRID_PADDING + i * CELL_SIZE;
                g.drawLine(x, GRID_PADDING, x, GRID_PADDING + GameBoard.SIZE * CELL_SIZE);
            }
        }

        private void drawPieces(Graphics2D g) {
            for (int r = 0; r < GameBoard.SIZE; r++) {
                for (int c = 0; c < GameBoard.SIZE; c++) {
                    int cell = board.getCell(r, c);
                    if (cell != GameBoard.EMPTY) {
                        int cx = GRID_PADDING + c * CELL_SIZE + CELL_SIZE / 2;
                        int cy = GRID_PADDING + r * CELL_SIZE + CELL_SIZE / 2;

                        if (cell == GameBoard.HUMAN) {
                            g.setColor(HUMAN_COLOR);
                        } else {
                            g.setColor(AI_COLOR);
                        }

                        // Draw filled circle
                        g.fillOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS,
                                   PIECE_RADIUS * 2, PIECE_RADIUS * 2);

                        // Draw border
                        g.setColor(Color.BLACK);
                        g.setStroke(new BasicStroke(1.5f));
                        g.drawOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS,
                                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);
                    }
                }
            }
        }

        private void drawRedLines(Graphics2D g) {
            List<GameBoard.RedLine> redLines = board.getRedLines();
            g.setColor(RED_LINE_COLOR);
            g.setStroke(new BasicStroke(3.0f));

            for (GameBoard.RedLine line : redLines) {
                if (line.horizontal) {
                    int y = GRID_PADDING + line.row * CELL_SIZE + CELL_SIZE / 2;
                    int x1 = GRID_PADDING + line.col * CELL_SIZE + CELL_SIZE / 2;
                    int x2 = GRID_PADDING + (line.col + line.length - 1) * CELL_SIZE + CELL_SIZE / 2;
                    g.drawLine(x1, y, x2, y);
                } else {
                    int x = GRID_PADDING + line.col * CELL_SIZE + CELL_SIZE / 2;
                    int y1 = GRID_PADDING + line.row * CELL_SIZE + CELL_SIZE / 2;
                    int y2 = GRID_PADDING + (line.row + line.length - 1) * CELL_SIZE + CELL_SIZE / 2;
                    g.drawLine(x, y1, x, y2);
                }
            }
        }
    }

    /**
     * Handle mouse click on the board
     */
    private void handleMouseClick(int x, int y) {
        if (engine.isGameOver()) {
            return;
        }

        if (engine.getCurrentPlayer() != GameBoard.HUMAN) {
            return;
        }

        if (aiThinking) {
            return;
        }

        // Convert pixel coordinates to grid coordinates
        int col = (x - GRID_PADDING) / CELL_SIZE;
        int row = (y - GRID_PADDING) / CELL_SIZE;

        // Check bounds
        if (row < 0 || row >= GameBoard.SIZE || col < 0 || col >= GameBoard.SIZE) {
            return;
        }

        // Check if cell is empty
        if (!board.isEmpty(row, col)) {
            return;
        }

        // Process human move
        processHumanMove(row, col);
    }

    /**
     * Auto-copy move history to clipboard (silently)
     */
    private void autoCopyHistory() {
        String text = board.getMoveHistoryString();
        if (text.isEmpty()) return;
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    /**
     * Undo (悔棋): reset board and replay from clipboard
     */
    private void undoMove() {
        // Read clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text;
        try {
            text = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (Exception e) {
            flashUndoButton();
            statusLabel.setText("⚠️ 无法读取剪贴板");
            return;
        }

        if (text == null || text.trim().isEmpty()) {
            flashUndoButton();
            statusLabel.setText("⚠️ 剪贴板为空，无法悔棋");
            return;
        }

        text = text.trim();

        // Validate format: e.g. "001h(0,0), 002a(1,1), 003h(2,2)"
        if (!text.matches("\\d{3}[ha]\\(\\d+,\\d+\\)(?:, \\d{3}[ha]\\(\\d+,\\d+\\))*")) {
            flashUndoButton();
            statusLabel.setText("⚠️ 剪贴板内容格式不符，无法悔棋");
            return;
        }

        // Parse moves
        String[] entries = text.split(", ");

        // Reset game completely (including move history)
        if (aiTimer != null) aiTimer.stop();
        aiThinking = false;
        engine.reset();
        board.clearMoveHistory();

        // Replay each move using processMoveRaw (no recording)
        for (String entry : entries) {
            char playerChar = entry.charAt(3);
            String coords = entry.substring(5, entry.length() - 1);
            String[] parts = coords.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            int player = (playerChar == 'h') ? GameBoard.HUMAN : GameBoard.AI;

            GameEngine.MoveResult result = engine.processMoveRaw(r, c, player);
            if (!result.valid) {
                statusLabel.setText("⚠️ 悔棋失败：记录 " + entry + " 无法重放");
                engine.reset();
                board.clearMoveHistory();
                refreshUI();
                return;
            }
            board.recordMove(r, c, player);
        }

        // After replay, set turn to human
        engine.setExtraTurn(false);
        engine.setCurrentPlayer(GameBoard.HUMAN);
        engine.setGameOver(false);
        refreshUI();
        statusLabel.setText("✅ 悔棋成功！已载入 " + entries.length + " 步记录，轮到人类");
    }

    private void flashUndoButton() {
        Color originalBg = undoButton.getBackground();
        undoButton.setBackground(Color.RED);
        Timer flashTimer = new Timer(500, e -> undoButton.setBackground(originalBg));
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    /**
     * Process a human player's move
     */
    private void processHumanMove(int row, int col) {
        // Auto-copy current history to clipboard before human moves
        autoCopyHistory();

        GameEngine.MoveResult result = engine.processMove(row, col, GameBoard.HUMAN);

        if (!result.valid) return;

        refreshUI();


        if (result.gameEnded) {
            showGameOver();
            return;
        }

        if (result.scored) {
            statusLabel.setText("人类得分 +" + result.score + "！获得额外回合！");
            // Human gets extra turn - wait for next click
        } else {
            statusLabel.setText("AI 思考中...");
            // Switch to AI
            engine.switchPlayer();
            // Trigger AI move with delay
            startAIMove();
        }
    }

    /**
     * Start AI move with 0.5 second delay
     */
    private void startAIMove() {
        aiThinking = true;
        aiTimer = new Timer(500, e -> {
            doAIMove();
        });
        aiTimer.setRepeats(false);
        aiTimer.start();
    }

    /**
     * Execute AI move
     */
    private void doAIMove() {
        if (engine.isGameOver()) return;

        int[] move = ai.getBestMove();
        if (move == null) return;

        int row = move[0];
        int col = move[1];

        GameEngine.MoveResult result = engine.processMove(row, col, GameBoard.AI);

        refreshUI();


        if (result.gameEnded) {
            showGameOver();
            aiThinking = false;
            return;
        }

        if (result.scored) {
            statusLabel.setText("AI 得分 +" + result.score + "！AI获得额外回合！");
            // AI gets extra turn
            aiTimer = new Timer(500, e -> doAIMove());
            aiTimer.setRepeats(false);
            aiTimer.start();
        } else {
            statusLabel.setText("轮到人类 (蓝色) - 请点击棋盘下子");
            engine.switchPlayer();
            aiThinking = false;
        }
    }

    /**
     * Update UI components
     */
    private void refreshUI() {
        humanScoreLabel.setText("人类: " + engine.getHumanScore() + " 分");
        aiScoreLabel.setText("AI: " + engine.getAiScore() + " 分");
        repaint();
    }


    /**
     * Show game over dialog
     */
    private void showGameOver() {
        int winner = engine.getWinner();
        String message;
        if (winner == GameBoard.HUMAN) {
            message = "🎉 人类获胜！\n\n人类: " + engine.getHumanScore() + " 分\nAI: " + engine.getAiScore() + " 分";
        } else if (winner == GameBoard.AI) {
            message = "🤖 AI 获胜！\n\n人类: " + engine.getHumanScore() + " 分\nAI: " + engine.getAiScore() + " 分";
        } else {
            message = "🤝 平局！\n\n双方: " + engine.getHumanScore() + " 分";
        }

        statusLabel.setText("游戏结束！");

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(parentFrame, message, "游戏结束", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    /**
     * Reset the game
     */
    private void resetGame() {
        if (aiTimer != null) {
            aiTimer.stop();
        }
        aiThinking = false;
        engine.reset();
        refreshUI();
        statusLabel.setText("人类先手 (蓝色) - 请点击棋盘下子");

    }
}
