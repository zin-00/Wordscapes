package main;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordscapesBackground extends JPanel {
    private List<FallingLetter> fallingLetters;
    private Timer animationTimer;
    private Random random;

    public WordscapesBackground() {
        fallingLetters = new ArrayList<>();
        random = new Random();

        // Initialize falling letters
        for (int i = 0; i < 100; i++) {
            fallingLetters.add(new FallingLetter());
        }

        // Animation timer to update and repaint
        animationTimer = new Timer(50, e -> {
            updateLetters();
            repaint();
        });
        animationTimer.start();

        setOpaque(false);
    }

    private void updateLetters() {
        for (FallingLetter letter : fallingLetters) {
            letter.update();
            if (letter.y > getHeight()) {
                letter.reset();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Gradient background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(200, 230, 255), 0, getHeight(), new Color(150, 200, 255));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw falling letters
        for (FallingLetter letter : fallingLetters) {
            letter.draw(g2d);
        }

        g2d.dispose();
    }

    private class FallingLetter {
        char letter;
        float x, y;
        float speed;
        float alpha;
        Font font;

        FallingLetter() {
            reset();
        }

        void reset() {
            // Randomly choose a letter
            letter = (char) ('A' + random.nextInt(26));
            
            // Random horizontal position, start above the screen
            x = random.nextFloat() * getWidth();
            y = -random.nextFloat() * getHeight() - 50;
            
            // Varied falling speed
            speed = 1 + random.nextFloat() * 2;
            
            // Varied transparency
            alpha = 0.1f + random.nextFloat() * 0.4f;
            
            // Varied font size
            int fontSize = 18 + random.nextInt(30);
            font = new Font("SansSerif", Font.BOLD, fontSize);
        }

        void update() {
            // Move letter downwards
            y += speed;
        }

        void draw(Graphics2D g2d) {
            // Set font and transparency
            g2d.setFont(font);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(Color.DARK_GRAY);
            
            // Draw the letter
            g2d.drawString(String.valueOf(letter), x, y);
        }
    }
}