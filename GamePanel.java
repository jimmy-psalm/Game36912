import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.ArrayList;

/**
 * GamePanel - Main Swing UI
 * Renders the board, handles mouse clicks, displays scores
 */
public class GamePanel extends JPanel {
    private GameBoard board;
    private GameEngine engine;
    private AIPlayer ai;

    // UI constants - Plan E colors
    private static final int CELL_SIZE = 48;
    private static final int GRID_PADDING = 30;
    private static final int PIECE_RADIUS = 18;
    private static final Color BOARD_COLOR = new Color(245, 230, 204); // #f5e6cc
    private static final Color GRID_LINE_COLOR = new Color(100, 80, 60);
    private static final Color HUMAN_COLOR = new Color(255, 107, 107); // #ff6b6b coral red
    private static final Color AI_COLOR = new Color(78, 205, 196); // #4ecdc4 teal
    private static final Color HUMAN_LINE_COLOR = new Color(255, 107, 107); // coral red for human scores
    private static final Color AI_LINE_COLOR = new Color(78, 205, 196); // teal for AI scores
    private static final Color BG_COLOR = new Color(240, 230, 210);

    // UI components
    private JLabel statusLabel;
    private JLabel humanScoreLabel;
    private JLabel aiScoreLabel;
    private JButton backButton;
    private JButton forwardButton;
    private JFrame parentFrame;
    
    // Game mode: true = basic (show hints), false = advance (no hints)
    private boolean basicMode = true;

    // AI thinking timer
    private Timer aiTimer;
    private boolean aiThinking;

    // Snapshots for forward/backward navigation
    private List<GameSnapshot> snapshots;
    private List<GameSnapshot> forwardSnapshots;

    public GamePanel(JFrame frame) {
        this.parentFrame = frame;
        this.board = new GameBoard();
        this.engine = new GameEngine(board);
        this.ai = new AIPlayer(board);
        this.aiThinking = false;
        this.snapshots = new ArrayList<>();
        this.forwardSnapshots = new ArrayList<>();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Top panel: title and status
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BG_COLOR);

        JLabel titleLabel = new JLabel("3-6-9-12 连线棋", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 40, 20));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        statusLabel = new JLabel("人类先手 (珊瑚红) - 请点击棋盘下子", SwingConstants.CENTER);
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

        // Bottom panel: scores and buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(BG_COLOR);

        humanScoreLabel = new JLabel("人类: 0 分");
        humanScoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        humanScoreLabel.setForeground(HUMAN_COLOR);

        aiScoreLabel = new JLabel("AI: 0 分");
        aiScoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        aiScoreLabel.setForeground(AI_COLOR);


        // Mode toggle button (single button: basic/advance toggle)
        JButton modeToggleBtn = new JButton("基础");
        modeToggleBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        modeToggleBtn.setBackground(new Color(90, 138, 181));
        modeToggleBtn.setForeground(Color.WHITE);
        modeToggleBtn.setFocusPainted(false);
        modeToggleBtn.addActionListener(e -> toggleMode(modeToggleBtn));

        JButton loadButton = new JButton("载入");
        loadButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        loadButton.setBackground(new Color(122, 154, 106));
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.addActionListener(e -> loadFromClipboard());

        // Board size button (cyclic: 144 → 81 → 36 → 144)
        JButton sizeButton = new JButton("144");
        sizeButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        sizeButton.setBackground(new Color(138, 122, 106));
        sizeButton.setForeground(Color.WHITE);
        sizeButton.setFocusPainted(false);
        sizeButton.addActionListener(e -> changeBoardSize(sizeButton));

        // Use a GridLayout-like approach: two rows
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        row1.setBackground(BG_COLOR);
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        row2.setBackground(BG_COLOR);
        
        // Row 1: copy, load, -+
        JButton copyBtn = new JButton("复制记录");
        copyBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        copyBtn.setBackground(new Color(90, 138, 181));
        copyBtn.setForeground(Color.WHITE);
        copyBtn.setFocusPainted(false);
        copyBtn.addActionListener(e -> copyHistory());
        
        JButton zoomOutBtn = new JButton("−");
        zoomOutBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        zoomOutBtn.setBackground(new Color(184, 168, 136));
        zoomOutBtn.setForeground(new Color(61, 43, 26));
        zoomOutBtn.setFocusPainted(false);
        zoomOutBtn.addActionListener(e -> resizeBoard(1 / 1.1));
        
        JButton zoomInBtn = new JButton("+");
        zoomInBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        zoomInBtn.setBackground(new Color(184, 168, 136));
        zoomInBtn.setForeground(new Color(61, 43, 26));
        zoomInBtn.setFocusPainted(false);
        zoomInBtn.addActionListener(e -> resizeBoard(1.1));
        
        JPanel zoomGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        zoomGroup.setBackground(BG_COLOR);
        zoomGroup.add(zoomOutBtn);
        zoomGroup.add(zoomInBtn);
        
        row1.add(copyBtn);
        row1.add(loadButton);
        row1.add(zoomGroup);
        
        // Row 2: <>, 144, basic/advance, start
        backButton = new JButton("←");
        backButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        backButton.setBackground(new Color(184, 168, 136));
        backButton.setForeground(new Color(61, 43, 26));
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> goBack());
        
        forwardButton = new JButton("→");
        forwardButton.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        forwardButton.setBackground(new Color(184, 168, 136));
        forwardButton.setForeground(new Color(61, 43, 26));
        forwardButton.setFocusPainted(false);
        forwardButton.addActionListener(e -> goForward());
        
        JPanel stepGroup = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        stepGroup.setBackground(BG_COLOR);
        stepGroup.add(backButton);
        stepGroup.add(forwardButton);
        
        JButton startBtn = new JButton("开始");
        startBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        startBtn.setBackground(new Color(200, 180, 150));
        startBtn.setForeground(new Color(61, 43, 26));
        startBtn.setFocusPainted(false);
        startBtn.addActionListener(e -> resetGame());
        
        row2.add(stepGroup);
        row2.add(sizeButton);
        row2.add(modeToggleBtn);
        row2.add(startBtn);
        
        bottomPanel.add(row1);
        bottomPanel.add(row2);
        
        // Also add score labels above the buttons
        bottomPanel.add(humanScoreLabel);
        bottomPanel.add(aiScoreLabel);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Toggle game mode between basic (show hints) and advance (no hints)
     */
    private void toggleMode(JButton modeToggleBtn) {
        basicMode = !basicMode;
        if (basicMode) {
            modeToggleBtn.setText("基础");
            modeToggleBtn.setBackground(new Color(90, 138, 181));
        } else {
            modeToggleBtn.setText("进阶");
            modeToggleBtn.setBackground(new Color(90, 138, 181));
        }
        repaint();
    }

    /**
     * Copy move history to clipboard
     */
    private void copyHistory() {
        String text = board.getMoveHistoryString();
        if (text.isEmpty()) {
            statusLabel.setText("⚠️ 暂无移动记录");
            return;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
        statusLabel.setText("✅ 已复制 " + board.getMoveHistory().size() + " 步移动记录到剪贴板");
    }

    /**
     * Resize the board by a zoom factor
     */
    private void resizeBoard(double factor) {
        // This is a simplified version - in a real app you'd adjust CELL_SIZE
        // For now, just repaint
        repaint();
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
            // Draw score lines BEFORE pieces so they appear behind the circles
            // This prevents lines from crossing numbers inside circles
            drawScoreLines(g2d);
            drawPieces(g2d);
            drawHints(g2d);
        }

        private void drawBoard(Graphics2D g) {
            g.setColor(GRID_LINE_COLOR);
            g.setStroke(new BasicStroke(1.5f));

            for (int i = 0; i <= GameBoard.SIZE; i++) {
                int y = GRID_PADDING + i * CELL_SIZE;
                g.drawLine(GRID_PADDING, y, GRID_PADDING + GameBoard.SIZE * CELL_SIZE, y);

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

                        g.fillOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS,
                                   PIECE_RADIUS * 2, PIECE_RADIUS * 2);

                        g.setColor(Color.BLACK);
                        g.setStroke(new BasicStroke(1.5f));
                        g.drawOval(cx - PIECE_RADIUS, cy - PIECE_RADIUS,
                                    PIECE_RADIUS * 2, PIECE_RADIUS * 2);
                    }
                }
            }
        }

        private void drawScoreLines(Graphics2D g) {
            List<GameBoard.RedLine> redLines = board.getRedLines();

            for (GameBoard.RedLine line : redLines) {
                // Use owner-specific color
                if (line.owner == GameBoard.HUMAN) {
                    g.setColor(HUMAN_LINE_COLOR);
                } else {
                    g.setColor(AI_LINE_COLOR);
                }
                // Thickness doubled from 3.0f to 6.0f
                g.setStroke(new BasicStroke(6.0f));

                if (line.horizontal) {
                    int y = GRID_PADDING + line.row * CELL_SIZE + CELL_SIZE / 2;
                    int x1 = GRID_PADDING + line.col * CELL_SIZE + CELL_SIZE / 2;
                    int x2 = GRID_PADDING + (line.col + line.length - 1) * CELL_SIZE + CELL_SIZE / 2;
                    // Connect circle edges, not centers - use PIECE_RADIUS + 5 to avoid crossing numbers
                    int offset = (x2 > x1) ? (PIECE_RADIUS + 5) : -(PIECE_RADIUS + 5);
                    g.drawLine(x1 + offset, y, x2 - offset, y);
                } else {
                    int x = GRID_PADDING + line.col * CELL_SIZE + CELL_SIZE / 2;
                    int y1 = GRID_PADDING + line.row * CELL_SIZE + CELL_SIZE / 2;
                    int y2 = GRID_PADDING + (line.row + line.length - 1) * CELL_SIZE + CELL_SIZE / 2;
                    int offset = (y2 > y1) ? (PIECE_RADIUS + 5) : -(PIECE_RADIUS + 5);
                    g.drawLine(x, y1 + offset, x, y2 - offset);
                }
            }
        }

        private void drawHints(Graphics2D g) {
            // Draw hints only in basic mode
            if (engine.getCurrentPlayer() == GameBoard.HUMAN && !engine.isGameOver() && !aiThinking && basicMode) {
                // Golden dotted circles: positions where human can score
                java.util.List<int[]> scoreHints = getHumanScoringHints();
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
                g.setColor(new Color(255, 170, 0)); // golden dotted circle
                for (int[] hint : scoreHints) {
                    int cx = GRID_PADDING + hint[1] * CELL_SIZE + CELL_SIZE / 2;
                    int cy = GRID_PADDING + hint[0] * CELL_SIZE + CELL_SIZE / 2;
                    g.drawOval(cx - PIECE_RADIUS - 4, cy - PIECE_RADIUS - 4,
                               (PIECE_RADIUS + 4) * 2, (PIECE_RADIUS + 4) * 2);
                }
                
                // Blue dotted circles: positions where AI cannot score (exclude golden hint positions)
                java.util.List<int[]> aiMinHints = getAIMinScoreHints();
                // Filter out positions that are already golden hints (human can score there)
                java.util.Set<String> scoreHintSet = new java.util.HashSet<>();
                for (int[] hint : scoreHints) {
                    scoreHintSet.add(hint[0] + "," + hint[1]);
                }
                java.util.List<int[]> filteredBlueHints = new java.util.ArrayList<>();
                for (int[] hint : aiMinHints) {
                    if (!scoreHintSet.contains(hint[0] + "," + hint[1])) {
                        filteredBlueHints.add(hint);
                    }
                }
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5, 5}, 0));
                g.setColor(new Color(68, 136, 255)); // blue dotted circle
                for (int[] hint : filteredBlueHints) {
                    int cx = GRID_PADDING + hint[1] * CELL_SIZE + CELL_SIZE / 2;
                    int cy = GRID_PADDING + hint[0] * CELL_SIZE + CELL_SIZE / 2;
                    g.drawOval(cx - PIECE_RADIUS - 4, cy - PIECE_RADIUS - 4,
                               (PIECE_RADIUS + 4) * 2, (PIECE_RADIUS + 4) * 2);
                }
                
                g.setStroke(new BasicStroke(1.5f));
            }
        }
    }

    /**
     * Find positions where human can score
     */
    private java.util.List<int[]> getHumanScoringHints() {
        java.util.List<int[]> hints = new ArrayList<>();
        int[] validLengths = {3, 6, 9, 12};
        
        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                if (!board.isEmpty(r, c)) continue;
                
                // Temporarily place human piece and check
                board.placePiece(r, c, GameBoard.HUMAN);
                
                // Check horizontal
                int hCount = countConsecutiveOccupied(r, c, true);
                // Check vertical
                int vCount = countConsecutiveOccupied(r, c, false);
                
                boolean canScore = false;
                for (int len : validLengths) {
                    if (hCount == len || vCount == len) {
                        canScore = true;
                        break;
                    }
                }
                
                board.removePiece(r, c);
                
                if (canScore) {
                    hints.add(new int[]{r, c});
                }
            }
        }
        return hints;
    }

    /**
     * Find positions where AI will have NO chance to gain point
     * For each empty cell, simulate human placing there, then check if AI
     * has ANY move that would score. Return cells where AI cannot score at all.
     */
    private java.util.List<int[]> getAIMinScoreHints() {
        java.util.List<int[]> emptyCells = new ArrayList<>();
        for (int r = 0; r < GameBoard.SIZE; r++) {
            for (int c = 0; c < GameBoard.SIZE; c++) {
                if (board.isEmpty(r, c)) {
                    emptyCells.add(new int[]{r, c});
                }
            }
        }
        if (emptyCells.isEmpty()) return emptyCells;
        
        java.util.List<int[]> safeCells = new ArrayList<>();
        int[] validLengths = {3, 6, 9, 12};
        
        for (int[] cell : emptyCells) {
            int r = cell[0], c = cell[1];
            // Temporarily place human piece here
            board.placePiece(r, c, GameBoard.HUMAN);
            
            // Check if AI has ANY move that would score
            boolean aiCanScore = false;
            for (int ar = 0; ar < GameBoard.SIZE && !aiCanScore; ar++) {
                for (int ac = 0; ac < GameBoard.SIZE && !aiCanScore; ac++) {
                    if (!board.isEmpty(ar, ac)) continue;
                    board.placePiece(ar, ac, GameBoard.AI);
                    int hCount = countConsecutiveOccupied(ar, ac, true);
                    int vCount = countConsecutiveOccupied(ar, ac, false);
                    boolean canScore = false;
                    for (int len : validLengths) {
                        if (hCount == len || vCount == len) {
                            canScore = true;
                            break;
                        }
                    }
                    board.removePiece(ar, ac);
                    if (canScore) {
                        aiCanScore = true;
                    }
                }
            }
            
            board.removePiece(r, c);
            
            // If AI cannot score at all, this is a safe cell
            if (!aiCanScore) {
                safeCells.add(new int[]{r, c});
            }
        }
        
        return safeCells;
    }

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

        int col = (x - GRID_PADDING) / CELL_SIZE;
        int row = (y - GRID_PADDING) / CELL_SIZE;

        if (row < 0 || row >= GameBoard.SIZE || col < 0 || col >= GameBoard.SIZE) {
            return;
        }

        if (!board.isEmpty(row, col)) {
            return;
        }

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
     * Load move history from clipboard and replay it
     */
    private void loadFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String text;
        try {
            text = (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            flashButton(backButton); // reuse flashButton for visual feedback
            statusLabel.setText("⚠️ 无法读取剪贴板");
            return;
        }
        
        if (text == null || text.trim().isEmpty()) {
            flashButton(backButton);
            statusLabel.setText("⚠️ 剪贴板为空，无法载入");
            return;
        }
        
        text = text.trim();
        
        // Parse the move history format: "001h(0,0), 002a(1,1), 003h(2,2)..."
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{3}[ha]\\(\\d+,\\d+\\))");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        java.util.List<String> matches = new java.util.ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        
        if (matches.isEmpty()) {
            flashButton(backButton);
            statusLabel.setText("⚠️ 剪贴板内容格式不正确，无法载入");
            return;
        }
        
        // Reset the game first
        if (aiTimer != null) {
            aiTimer.stop();
        }
        aiThinking = false;
        engine.reset();
        snapshots.clear();
        forwardSnapshots.clear();
        
        // Parse and replay each move
        boolean success = true;
        for (int i = 0; i < matches.size(); i++) {
            String entry = matches.get(i);
            char playerChar = entry.charAt(3);
            String coords = entry.substring(5, entry.length() - 1);
            String[] parts = coords.split(",");
            int r = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            int player = (playerChar == 'h') ? GameBoard.HUMAN : GameBoard.AI;
            
            if (r < 0 || r >= GameBoard.SIZE || c < 0 || c >= GameBoard.SIZE || !board.isEmpty(r, c)) {
                success = false;
                break;
            }
            
            // Take snapshot before each move
            takeSnapshot();
            
            GameEngine.MoveResult result = engine.processMove(r, c, player);
            if (!result.valid) {
                success = false;
                break;
            }
            
            board.recordMove(r, c, player);
        }
        
        if (!success) {
            flashButton(backButton);
            engine.reset();
            snapshots.clear();
            forwardSnapshots.clear();
            refreshUI();
            statusLabel.setText("⚠️ 载入失败，记录格式有误");
            return;
        }
        
        // After loading all moves, ensure it's human's turn
        if (engine.getCurrentPlayer() == GameBoard.AI) {
            engine.setCurrentPlayer(GameBoard.HUMAN);
        }
        engine.setExtraTurn(false);
        
        refreshUI();
        statusLabel.setText("✅ 已载入 " + matches.size() + " 步记录，请使用 ← → 回放");
        autoCopyHistory();
    }

    /**
     * Change board size cyclically: 144 → 81 → 36 → 144
     */
    private void changeBoardSize(JButton sizeButton) {
        // Disable during active game (pieces placed but game not over)
        if (!board.getMoveHistory().isEmpty() && !engine.isGameOver()) {
            flashButton(sizeButton);
            statusLabel.setText("⚠️ 游戏进行中无法切换棋盘大小，请先重置");
            return;
        }
        
        String currentText = sizeButton.getText();
        int newSize;
        if (currentText.equals("144")) {
            sizeButton.setText("81");
            newSize = 9;
        } else if (currentText.equals("81")) {
            sizeButton.setText("36");
            newSize = 6;
        } else {
            sizeButton.setText("144");
            newSize = 12;
        }
        
        // Update board size
        board.setSize(newSize);
        engine.reset();
        snapshots.clear();
        forwardSnapshots.clear();
        
        // Update board panel size
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof BoardPanel) {
                BoardPanel bp = (BoardPanel) comp;
                bp.setPreferredSize(new Dimension(
                    GRID_PADDING * 2 + CELL_SIZE * newSize,
                    GRID_PADDING * 2 + CELL_SIZE * newSize
                ));
                bp.revalidate();
                break;
            }
        }
        
        refreshUI();
        statusLabel.setText("棋盘已切换为 " + newSize + "x" + newSize + " (" + sizeButton.getText() + " 格)");
    }

    /**
     * Take a snapshot of current game state
     */
    private void takeSnapshot() {
        snapshots.add(new GameSnapshot(board, engine));
    }

    /**
     * Go back one step (skip AI steps until human turn)
     */
    private void goBack() {
        if (snapshots.isEmpty()) {
            flashButton(backButton);
            statusLabel.setText("⚠️ 已经是最初状态，无法后退");
            return;
        }

        // Save current state to forwardSnapshots
        forwardSnapshots.add(new GameSnapshot(board, engine));

        // Restore previous snapshot
        GameSnapshot snap = snapshots.remove(snapshots.size() - 1);
        snap.restore(board, engine);

        // Skip AI steps - keep going back until human's turn
        while (engine.getCurrentPlayer() == GameBoard.AI && !snapshots.isEmpty()) {
            forwardSnapshots.add(new GameSnapshot(board, engine));
            snap = snapshots.remove(snapshots.size() - 1);
            snap.restore(board, engine);
        }

        refreshUI();
        statusLabel.setText("✅ 已后退到第 " + board.getMoveHistory().size() + " 步，轮到人类");
        autoCopyHistory();
    }

    /**
     * Go forward one step (skip AI steps until human turn)
     */
    private void goForward() {
        if (forwardSnapshots.isEmpty()) {
            flashButton(forwardButton);
            statusLabel.setText("⚠️ 已经是最新状态，无法前进");
            return;
        }

        // Save current state to snapshots
        snapshots.add(new GameSnapshot(board, engine));

        // Restore next forward snapshot
        GameSnapshot snap = forwardSnapshots.remove(forwardSnapshots.size() - 1);
        snap.restore(board, engine);

        // Skip AI steps - keep going forward until human's turn
        while (engine.getCurrentPlayer() == GameBoard.AI && !forwardSnapshots.isEmpty()) {
            snapshots.add(new GameSnapshot(board, engine));
            snap = forwardSnapshots.remove(forwardSnapshots.size() - 1);
            snap.restore(board, engine);
        }

        refreshUI();
        statusLabel.setText("✅ 已前进到第 " + board.getMoveHistory().size() + " 步，轮到人类");
        autoCopyHistory();
    }

    private void flashButton(JButton btn) {
        Color originalBg = btn.getBackground();
        btn.setBackground(Color.RED);
        Timer flashTimer = new Timer(500, e -> btn.setBackground(originalBg));
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    /**
     * Process a human player's move
     */
    private void processHumanMove(int row, int col) {
        // Auto-copy current history to clipboard before human moves
        autoCopyHistory();

        // Take snapshot before the move
        takeSnapshot();
        // Clear forward snapshots since we're making a new move
        forwardSnapshots.clear();

        GameEngine.MoveResult result = engine.processMove(row, col, GameBoard.HUMAN);

        if (!result.valid) return;

        refreshUI();

        if (result.gameEnded) {
            showGameOver();
            return;
        }

        if (result.scored) {
            statusLabel.setText("人类得分 +" + result.score + "！获得额外回合！");
        } else {
            statusLabel.setText("AI 思考中...");
            engine.switchPlayer();
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

        // Take snapshot before AI move
        takeSnapshot();
        forwardSnapshots.clear();

        GameEngine.MoveResult result = engine.processMove(row, col, GameBoard.AI);

        refreshUI();

        if (result.gameEnded) {
            showGameOver();
            aiThinking = false;
            return;
        }

        if (result.scored) {
            statusLabel.setText("AI 得分 +" + result.score + "！AI获得额外回合！");
            aiTimer = new Timer(500, e -> doAIMove());
            aiTimer.setRepeats(false);
            aiTimer.start();
        } else {
            statusLabel.setText("轮到人类 (珊瑚红) - 请点击棋盘下子");
            engine.switchPlayer();
            aiThinking = false;
            // Auto-copy history when it becomes human's turn
            autoCopyHistory();
            // Repaint immediately to show hints
            repaint();
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
        snapshots.clear();
        forwardSnapshots.clear();
        refreshUI();
        statusLabel.setText("人类先手 (珊瑚红) - 请点击棋盘下子");
    }

    /**
     * GameSnapshot - stores complete game state for forward/backward navigation
     */
    private static class GameSnapshot {
        private int[][] grid;
        private List<GameBoard.RedLine> redLines;
        private int humanScore;
        private int aiScore;
        private int currentPlayer;
        private boolean gameOver;
        private boolean extraTurn;
        private List<String> moveHistory;
        private int moveNumber;

        public GameSnapshot(GameBoard board, GameEngine engine) {
            // Deep copy grid
            this.grid = new int[GameBoard.SIZE][GameBoard.SIZE];
            for (int r = 0; r < GameBoard.SIZE; r++) {
                for (int c = 0; c < GameBoard.SIZE; c++) {
                    this.grid[r][c] = board.getCell(r, c);
                }
            }

            // Deep copy red lines
            this.redLines = new ArrayList<>();
            for (GameBoard.RedLine line : board.getRedLines()) {
                this.redLines.add(new GameBoard.RedLine(
                    line.row, line.col, line.horizontal, line.length, line.owner
                ));
            }

            this.humanScore = engine.getHumanScore();
            this.aiScore = engine.getAiScore();
            this.currentPlayer = engine.getCurrentPlayer();
            this.gameOver = engine.isGameOver();
            this.extraTurn = engine.hasExtraTurn();
            this.moveHistory = new ArrayList<>(board.getMoveHistory());
            this.moveNumber = moveHistory.size();
        }

        public void restore(GameBoard board, GameEngine engine) {
            // Reset everything
            engine.reset();
            board.reset();
            
            // Restore grid directly
            for (int r = 0; r < GameBoard.SIZE; r++) {
                for (int c = 0; c < GameBoard.SIZE; c++) {
                    if (grid[r][c] != GameBoard.EMPTY) {
                        board.placePiece(r, c, grid[r][c]);
                    }
                }
            }
            
            // Restore red lines
            for (GameBoard.RedLine line : redLines) {
                board.addRedLine(line);
            }
            
            // Restore move history
            for (String entry : moveHistory) {
                board.recordMove(0, 0, GameBoard.HUMAN); // dummy - we'll fix moveNumber
            }
            // Clear and re-add properly
            board.clearMoveHistory();
            for (String entry : moveHistory) {
                char playerChar = entry.charAt(3);
                String coords = entry.substring(5, entry.length() - 1);
                String[] parts = coords.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                int player = (playerChar == 'h') ? GameBoard.HUMAN : GameBoard.AI;
                board.recordMove(r, c, player);
            }
            
            // Restore scores directly using setters
            engine.setHumanScore(humanScore);
            engine.setAiScore(aiScore);
            
            // Set state
            engine.setCurrentPlayer(currentPlayer);
            engine.setExtraTurn(extraTurn);
            engine.setGameOver(gameOver);
        }
    }
}
