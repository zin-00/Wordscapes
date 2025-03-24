package main;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class WordBox extends JPanel {
    private final List<JPanel> letterBoxes;
    private boolean isFilled;
    private Set<Integer> revealedLetterPositions = new HashSet<>();
    private static final int LETTER_BOX_SIZE = 50;
    private static final Color MAIN_COLOR = new Color(0, 123, 255); // Bootstrap primary color
    private static final Color HINT_COLOR = new Color(220, 53, 69, 100); // Bootstrap danger color with transparency
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250); // Bootstrap light color
    private int revealedLetterCount = 0;
    private String word;

    public WordBox(String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be null or empty.");
        }

        this.word = word;

        setLayout(new BorderLayout(10, 10));
        setOpaque(false); // Make the panel transparent

        JPanel boxesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        boxesPanel.setOpaque(false); // Make the panel transparent
        letterBoxes = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            JPanel letterBox = createLetterBox();
            letterBoxes.add(letterBox);
            boxesPanel.add(letterBox);
        }

        add(boxesPanel, BorderLayout.CENTER);
        isFilled = false;
    }

    private JPanel createLetterBox() {
        JPanel letterBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BACKGROUND_COLOR);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.setColor(MAIN_COLOR);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.dispose();
            }
        };
        letterBox.setPreferredSize(new Dimension(LETTER_BOX_SIZE, LETTER_BOX_SIZE));
        letterBox.setOpaque(false);
        return letterBox;
    }

    public void fillWord(String word) {
        for (int i = 0; i < word.length(); i++) {
            revealLetter(i, word.charAt(i));
        }
        isFilled = true;
    }

    public void revealLetter(int position, char letter, boolean isHint) {
        if (position >= 0 && position < letterBoxes.size()) {
            JPanel letterBox = letterBoxes.get(position);
            letterBox.removeAll();
            letterBox.setLayout(new BorderLayout());
            JLabel letterLabel = new JLabel(String.valueOf(letter), SwingConstants.CENTER);
            letterLabel.setForeground(isHint ? HINT_COLOR : MAIN_COLOR);
            letterLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            letterBox.add(letterLabel);
            revealedLetterPositions.add(position);
            revealedLetterCount++;
            revalidate();
            repaint();
        }
    }

    public boolean isLetterRevealed(int position) {
        return revealedLetterPositions.contains(position);
    }

    public void revealLetter(int position, char letter) {
        revealLetter(position, letter, false);
    }

    public int getWordLength() {
        return letterBoxes.size();
    }

    public boolean isFilled() {
        return isFilled;
    }

    public void setFilled(boolean filled) {
        isFilled = filled;
    }

    public void reset() {
        for (JPanel letterBox : letterBoxes) {
            letterBox.removeAll();
            letterBox.revalidate();
            letterBox.repaint();
        }
        revealedLetterPositions.clear();
        revealedLetterCount = 0;
        isFilled = false;
        revalidate();
        repaint();
    }

    public void revealLettersWithDelay() {
        Timer revealTimer = new Timer(500, new ActionListener() {
            private int currentPos = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentPos < word.length()) {
                    revealLetter(currentPos, word.charAt(currentPos), false);
                    currentPos++;
                } else {
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        revealTimer.start();
    }

    public int getRevealedLetterCount() {
        return revealedLetterCount;
    }

    public Set<Integer> getRevealedLetterPositions() {
        return revealedLetterPositions;
    }
}

