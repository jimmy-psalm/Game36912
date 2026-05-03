import javax.swing.*;
import java.awt.*;

/**
 * Game36912 - Main entry point
 * 3-6-9-12 连线棋 (人机对战版)
 */
public class Game36912 {
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create the main frame
        JFrame frame = new JFrame("3-6-9-12 连线棋");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        // Create and add the game panel
        GamePanel gamePanel = new GamePanel(frame);
        frame.add(gamePanel);

        // Pack and center on screen
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
