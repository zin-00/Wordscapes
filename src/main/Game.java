package main;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

public class Game extends JFrame {
    // --- UI Colors and Styling ---
    private static final Color MAIN_COLOR = new Color(108, 142, 191);
    private static final Color BACKGROUND_COLOR = new Color(240, 245, 255);
    private static final Color CORRECT_COLOR = new Color(109, 191, 137);
    private static final Color INCORRECT_COLOR = new Color(212, 90, 80);
    private static final Color CIRCLE_COLOR = new Color(202, 184, 227);
    private static final Color ACCENT_COLOR = new Color(255, 111, 97);
    private static final int PADDING = 40;

    // --- Game State ---
    private String currentWord = "";
    private int score = 0;
    private int currentLevel = 0;
    int timeRemaining = 60;
    int attempts = 0;
    private static final int maxAttempts = 3;

    // --- Data Structures for Game Logic ---
    private List<JButton> letterButtons;
    private Map<String, WordBox> wordBoxes;
    public Set<String> validWords;
    private List<GameLevel> levels;
    public Map<Integer, WordBox> wordBoxesByLength;

    // --- Game Level and AI ---
    private GameLevel glevel;
    private GameAI gameAI;

    // --- UI Components ---
    private JPanel circlePanel;
    private JPanel wordBoxesPanel;
    private JPanel historyPanel;
    private JTextField currentWordInput;

    // --- Labels for Game Stats ---
    private JLabel levelLabel;
    private JLabel pointsLabel;
    private JLabel timeLabel;
    private JLabel attemptsLabel;

    // --- Word History ---
    private DefaultListModel<WordAttempt> wordHistoryModel;
    private JList<WordAttempt> wordHistoryList;
    private SoundManager sound;

    private WordscapesBackground background;
    private Menu menu;

    private static class WordAttempt {
        String word;
        boolean correct;

        WordAttempt(String word, boolean correct) {
            this.word = word;
            this.correct = correct;
        }
        @Override
        public String toString() {return word;}
    }

    // Constructor
    public Game(Menu menu) {
        this.sound = new SoundManager();
        wordBoxesByLength = new HashMap<>();
        glevel = new GameLevel("", new HashSet<>());
        this.menu = menu;
        glevel.initializeLevels();
        levels = glevel.getLevels();
        gameAI = new GameAI();
        wordHistoryModel = new DefaultListModel<>();
        
        initializeUI();
        loadLevel(currentLevel);
        startTimer();
        setVisible(true);
        
        gameAI.setGame(this);
    }

    private void initializeUI() {
        setTitle("Wordscapes Puzzle");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Set the frame to be undecorated (no title bar)
        setUndecorated(true);

        // Get the default toolkit
        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Get the screen size
        Dimension screenSize = toolkit.getScreenSize();

        // Set the size of the frame to the screen size
        setSize(screenSize);

        // Set the location to the top-left corner
        setLocation(0, 0);

        // Create and set the WordscapesBackground
        background = new WordscapesBackground();
        setContentPane(background);
        setLayout(new BorderLayout(10, 10));

        letterButtons = new ArrayList<>();
        wordBoxes = new HashMap<>();

        createGamePanels();

        // Set the frame to fullscreen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            System.out.println("Full screen is not supported on this device. Using maximized window instead.");
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        // Add key listener to exit fullscreen mode
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    gd.setFullScreenWindow(null);
                    dispose();
                    System.exit(0);
                }
            }
        });

        // Ensure the frame can receive key events
        setFocusable(true);
        requestFocus();
    }
    void createGamePanels() {
        JPanel mainContainer = new JPanel(new BorderLayout(20, 20));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create top panel container
        JPanel topPanelContainer = new JPanel(new BorderLayout(20, 20));
        topPanelContainer.setOpaque(false);

        // Create scoreboard panel
        JPanel scoreboardPanel = createScoreboardPanel();
        topPanelContainer.add(scoreboardPanel, BorderLayout.CENTER);

        // Create left side panel that contains both word boxes, input field, and history
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(500, 10));

        // Word boxes panel
        wordBoxesPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        wordBoxesPanel.setOpaque(false);

        // Create history panel
        createHistoryPanel();

        // Create input field panel
        JPanel wordInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wordInputPanel.setOpaque(false);
        currentWordInput = new JTextField();
        currentWordInput.setPreferredSize(new Dimension(500, 30));
        currentWordInput.setFont(new Font("Arial", Font.BOLD, 18));
        currentWordInput.setEditable(true);
        wordInputPanel.add(currentWordInput);

        // Add components to left panel
        leftPanel.add(wordBoxesPanel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(wordInputPanel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(historyPanel);

        createCirclePanel();

        // Add components to main container
        mainContainer.add(topPanelContainer, BorderLayout.NORTH);
        mainContainer.add(leftPanel, BorderLayout.WEST);
        mainContainer.add(circlePanel, BorderLayout.CENTER);

        add(mainContainer);
    }

    private JPanel createScoreboardPanel() {
        JPanel scoreboardPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        scoreboardPanel.setOpaque(false);
        
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setForeground(Color.WHITE);
        backButton.setBackground(new Color(178, 34, 34));
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        backButton.addActionListener(e -> returnToMainMenu());

        scoreboardPanel.add(backButton);

        levelLabel = createStyledLabel("LEVEL 1", new Color(70, 130, 180));
        pointsLabel = createStyledLabel("POINTS: 0", new Color(46, 139, 87));
        timeLabel = createStyledLabel("TIME: 01:00", new Color(255, 140, 0));
        attemptsLabel = createStyledLabel("HINTS: 0/3", new Color(178, 34, 34));

        scoreboardPanel.add(levelLabel);
        scoreboardPanel.add(pointsLabel);
        scoreboardPanel.add(timeLabel);
        scoreboardPanel.add(attemptsLabel);

        return scoreboardPanel;
    }
    private void returnToMainMenu() {
        // Stop any ongoing timers or threads
        // Clear game state
        score = 0;
        currentLevel = 0;
        currentWord = "";
        timeRemaining = 60;
        attempts = 0;

        // Clear UI components
        clearLevel();
        updatePointsDisplay();
        updateLevelDisplay();
        updateTimeDisplay();
        updateAttemptsDisplay();

        // Show main menu
        SwingUtilities.invokeLater(() -> {
            menu.setVisible(true); // Show the menu
            this.dispose(); // Dispose of the current game window
        });
    }

    private JLabel createStyledLabel(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(color);
        label.setBackground(new Color(255, 255, 255, 200));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return label;
    }

    private void createHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setOpaque(false);
        historyPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(MAIN_COLOR),
            "Word History",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            MAIN_COLOR
        ));

        wordHistoryList = new JList<>(wordHistoryModel);
        wordHistoryList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

                WordAttempt attempt = (WordAttempt) value;
                label.setForeground(attempt.correct ? CORRECT_COLOR : INCORRECT_COLOR);
                label.setFont(new Font("Arial", Font.PLAIN, 14));
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(wordHistoryList);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        historyPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createCirclePanel() {
        circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(255, 255, 255, 150));
                int diameter = Math.min(getWidth(), getHeight()) - (2 * PADDING);
                int x = (getWidth() - diameter) / 2;
                int y = (getHeight() - diameter) / 2;
                g2d.fillOval(x, y, diameter, diameter);

                g2d.setColor(MAIN_COLOR);
                g2d.setStroke(new BasicStroke(3f));
                g2d.drawOval(x, y, diameter, diameter);

                setOpaque(false);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 300);
            }
        };
        circlePanel.setLayout(null);

        circlePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionLetterButtons();
            }
        });
    }

    private void loadLevel(int levelIndex) {
        if (levelIndex >= levels.size()) {
            gameComplete();
            return;
        }

        GameLevel level = levels.get(levelIndex);
        validWords = level.getValidWords();
        gameAI.initializeLevel(validWords, levelIndex + 1, wordBoxesByLength);
        clearLevel();
        createWordBoxesForLevel();

        List<Character> shuffledLetters = new ArrayList<>();
        for (char c : level.getLetters().toCharArray()) {
            shuffledLetters.add(c);
        }
        Collections.shuffle(shuffledLetters);

        for (char c : shuffledLetters) {
            JButton letterButton = createLetterButton(String.valueOf(c));
            letterButton.setSize(60, 60);
            circlePanel.add(letterButton);
            letterButtons.add(letterButton);
        }

        repositionLetterButtons();
        updateLevelDisplay();
        attempts = 0;
        resetTimer();
        resetHintsCounter();
        revalidate();
        repaint();
    }

    private void clearLevel() {
        circlePanel.removeAll();
        wordBoxesPanel.removeAll();
        letterButtons.clear();
        wordBoxes.clear();
        currentWord = "";
        currentWordInput.setText("");
    }

    private void createWordBoxesForLevel() {
        wordBoxesByLength.clear();

        Map<Integer, List<String>> wordsByLength = new HashMap<>();

        for (String word : validWords) {
            wordsByLength.computeIfAbsent(word.length(), k -> new ArrayList<>()).add(word);
        }

        for (Map.Entry<Integer, List<String>> entry : wordsByLength.entrySet()) {
            int length = entry.getKey();
            List<String> wordsOfLength = entry.getValue();

            WordBox wordBox = new WordBox(wordsOfLength.get(0));
            wordBoxesByLength.put(length, wordBox);

            wordBoxesPanel.add(wordBox);

            for (String word : wordsOfLength) {
                wordBoxes.put(word, wordBox);
            }
        }
    }

    private JButton createLetterButton(String letter) {
        JButton button = new JButton(letter);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 130, 180, 200));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.addActionListener(e -> addLetter(letter));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(100, 160, 210, 220));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(70, 130, 180, 200));
            }
        });

        return button;
    }

    void repositionLetterButtons() {
        if (letterButtons.isEmpty()) {
            return;
        }

        int padding = 40;
        int width = circlePanel.getWidth();
        int height = circlePanel.getHeight();
        int diameter = Math.min(width, height) - (2 * padding);
        int centerX = width / 2;
        int centerY = height / 2;
        int radius = diameter / 2 - 30;

        for (int i = 0; i < letterButtons.size(); i++) {
            JButton button = letterButtons.get(i);
            double angle = 2 * Math.PI * i / letterButtons.size() - Math.PI / 2;
            int x = (int) (centerX + radius * Math.cos(angle)) - button.getWidth() / 2;
            int y = (int) (centerY + radius * Math.sin(angle)) - button.getHeight() / 2;
            button.setLocation(x, y);
        }
    }

    private void addLetter(String letter) {
        currentWord += letter;
        currentWordInput.setText(currentWord);
        if (currentWord.length() >= 3) {
            checkWord();
        }
    }

    private void checkWord() {
        if (currentWord.length() < 3) {
            return;
        }
        boolean isValidWord = validWords.contains(currentWord);

        wordHistoryModel.addElement(new WordAttempt(currentWord, isValidWord));

        wordHistoryList.ensureIndexIsVisible(wordHistoryModel.getSize() - 1);

        if (isValidWord) {
            WordBox wordBox = wordBoxes.get(currentWord);
            if (wordBox != null && !wordBox.isFilled()) {
                wordBox.fillWord(currentWord);
                wordBox.setFilled(true);

                int score = gameAI.calculateScore(currentWord, gameAI.getHintsUsed() > 0);
                this.score += score;
                updatePointsDisplay();

                validWords.remove(currentWord);
                gameAI.wordSolved(currentWord);

                currentWord = "";
                currentWordInput.setText("");

                if (wordBoxesByLength.values().stream().allMatch(WordBox::isFilled)) {
                    showLevelCompleteDialog();
                }
            }
        } else {
            boolean isValidPrefix = false;
            for (String word : validWords) {
                if (word.startsWith(currentWord)) {
                    isValidPrefix = true;
                    break;
                }
            }

            if (!isValidPrefix) {
                currentWord = "";
                currentWordInput.setText("");
            }
        }
    }
    private void showLevelCompleteDialog() {
        // Get the root pane's layered pane to manage overlay
        JLayeredPane layeredPane = getRootPane().getLayeredPane();

        // Overlay panel with gradient background
        JPanel overlayPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(70, 130, 180, 230),
                    getWidth(), getHeight(), new Color(100, 149, 237, 230)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlayPanel.setLayout(new GridBagLayout());
        overlayPanel.setOpaque(false);
        overlayPanel.setBounds(0, 0, getWidth(), getHeight());

        // Content panel with modern design
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Rounded white background with shadow
                g2d.setColor(new Color(255, 255, 255, 240));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2d.setColor(new Color(0, 0, 0, 30));
                for (int i = 0; i < 5; i++) {
                    g2d.drawRoundRect(-i, -i, getWidth() + 2 * i, getHeight() + 2 * i, 30 + 2 * i, 30 + 2 * i);
                }
            }
        };
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(400, 300));

        // Animated celebration icon
        JLabel celebrationIcon = new JLabel("🎉");
        celebrationIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 80));
        celebrationIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title label
        JLabel titleLabel = new JLabel("Level Complete!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Score panel
        JPanel scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));

        JLabel scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
        scoreLabel.setForeground(new Color(46, 139, 87));

        JLabel levelLabel = new JLabel("Level: " + (currentLevel + 1));
        levelLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        levelLabel.setForeground(new Color(100, 100, 100));

        scorePanel.add(scoreLabel);
        scorePanel.add(Box.createVerticalStrut(10));
        scorePanel.add(levelLabel);

        // Continue button
        JButton continueButton = new JButton("Continue") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(new Color(50, 120, 170));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(80, 150, 200));
                } else {
                    g2d.setColor(new Color(70, 130, 180));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g);
            }
        };
        continueButton.setFont(new Font("Arial", Font.BOLD, 18));
        continueButton.setForeground(Color.WHITE);
        continueButton.setContentAreaFilled(false);
        continueButton.setBorderPainted(false);
        continueButton.setFocusPainted(false);
        continueButton.setPreferredSize(new Dimension(200, 50));
        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Continue button action
        continueButton.addActionListener(e -> {
            layeredPane.remove(overlayPanel);
            currentLevel++;
            loadLevel(currentLevel);
            revalidate();
            repaint();
        });

        // Assemble content panel
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(celebrationIcon);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(scorePanel);
        contentPanel.add(Box.createVerticalStrut(30));
        contentPanel.add(continueButton);

        // Add content to overlay
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        overlayPanel.add(contentPanel, gbc);

        // Add the overlay panel to the layered pane
        layeredPane.add(overlayPanel, JLayeredPane.POPUP_LAYER);
        overlayPanel.requestFocusInWindow();

        // ESC key to close overlay
        overlayPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    layeredPane.remove(overlayPanel);
                    currentLevel++;
                    loadLevel(currentLevel);
                    revalidate();
                    repaint();
                }
            }
        });
        overlayPanel.setFocusable(true);
        overlayPanel.requestFocusInWindow();
    }

    // Utility method for creating styled buttons
    private JButton createStyledButton(String text, Dimension size, Color bgColor, Color fgColor, Font font) {
        JButton button = new JButton(text);
        button.setPreferredSize(size);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(font);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void gameComplete() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("🎉 Congratulations! 🎉");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel completionLabel = new JLabel("You've completed all levels!");
        completionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        completionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Final Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(new Color(46, 139, 87));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel attemptsLabel = new JLabel("Hints Used: " + gameAI.getHintsUsed());
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        attemptsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        Dimension buttonSize = new Dimension(150, 40);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        JDialog dialog = new JDialog(this, "Game Complete", true);

        JButton playAgainButton = createStyledButton("Play Again", buttonSize,
            new Color(70, 130, 180), Color.WHITE, buttonFont);
        playAgainButton.addActionListener(e -> {
            dialog.dispose();
            score = 0;
            currentLevel = 0;
            gameAI = new GameAI();
            restart();
            loadLevel(currentLevel);
        });

        JButton exitButton = createStyledButton("Exit", buttonSize,
            new Color(180, 70, 70), Color.WHITE, buttonFont);
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(completionLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(attemptsLabel);
        panel.add(Box.createVerticalStrut(25));
        buttonPanel.add(playAgainButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel);

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }
  private void gameOver() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Game Over");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(180, 70, 70));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel levelLabel = new JLabel("Level " + (currentLevel + 1));
        levelLabel.setFont(new Font("Arial", Font.BOLD, 18));
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Final Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        scoreLabel.setForeground(new Color(70, 130, 180));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        Dimension buttonSize = new Dimension(150, 40);
        Font buttonFont = new Font("Arial", Font.BOLD, 14);

        JButton retryButton = createStyledButton("Retry Level", buttonSize,
            new Color(70, 130, 180), Color.WHITE, buttonFont);
        retryButton.addActionListener(e -> {
            Window dialog = SwingUtilities.getWindowAncestor(panel);
            dialog.dispose();
            restart();
        });

        JButton newGameButton = createStyledButton("New Game", buttonSize,
            new Color(46, 139, 87), Color.WHITE, buttonFont);
        newGameButton.addActionListener(e -> {
            Window dialog = SwingUtilities.getWindowAncestor(panel);
            dialog.dispose();
            restart();
            currentLevel = 0;
            loadLevel(currentLevel);
        });

        JButton exitButton = createStyledButton("Exit", buttonSize,
            new Color(180, 70, 70), Color.WHITE, buttonFont);
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(Box.createVerticalStrut(10));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(levelLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(25));
        buttonPanel.add(retryButton);
        buttonPanel.add(newGameButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel);

        JDialog dialog = new JDialog(this, "Game Over", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private void restart() {
        score = 0;
        updatePointsDisplay();
        wordBoxesByLength.clear();
        loadLevel(currentLevel);
        attempts = 0;
        updateAttemptsDisplay();
        resetTimer();
        updateTimeDisplay();
        currentWordInput.setText("");
    }

    private void startTimer() {
        Timer timer = new Timer(1000, e -> {
            timeRemaining--;
            updateTimeDisplay();

            if (timeRemaining <= 0) {
                attempts++;
                updateAttemptsDisplay();
                if (attempts >= maxAttempts) {
                   gameOver();
                } else {
                    gameAI.provideHint();
                    resetTimer();
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }

    private void resetTimer() {
        timeRemaining = 60;
        updateTimeDisplay();
    }

    private void resetHintsCounter() {
        gameAI.resetHintsCounter();
        attempts = 0;
        updateAttemptsDisplay();
    }

    void updateTimeDisplay() {
        int minutes = timeRemaining / 60;
        int seconds = timeRemaining % 60;
        String timeString = String.format("%02d:%02d", minutes, seconds);
        timeLabel.setText("TIME: " + timeString);
    }

    private void updatePointsDisplay() {
        pointsLabel.setText("POINTS: " + score);
    }

    private void updateLevelDisplay() {
        levelLabel.setText("LEVEL: " + (currentLevel + 1));
    }

    void updateAttemptsDisplay() {
        attemptsLabel.setText("HINTS: " + attempts + "/3");
    }
    
}