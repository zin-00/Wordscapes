package main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;

public class Menu extends JFrame implements ActionListener {
    private JButton playButton;
    private JButton settingsButton;
    private JButton exitButton;
    private JLabel titleLabel;
    private Timer fadeInTimer;
    private float alpha = 0f;
    private WordscapesBackground backgroundPanel;
    private SoundManager sound;
    private Game game;

    public Menu(Game game) {
        // Create background panel
        backgroundPanel = new WordscapesBackground();
        this.game = game;
        
        // Set content pane to the background panel with layout
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(new GridBagLayout());
        sound = new SoundManager();

        initializeUI();
        startTitleFadeIn();
    }

    private void initializeUI() {
    	sound.playBackgroundMusic();
        setTitle("Wordscapes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        titleLabel = createTitleLabel();
        playButton = createStyledButton("Play", "play");
        settingsButton = createStyledButton("Settings", "settings");
        exitButton = createStyledButton("Exit", "exit");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 50, 0);
        backgroundPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(10, 0, 10, 0);
        backgroundPanel.add(playButton, gbc);

        gbc.gridy++;
        backgroundPanel.add(settingsButton, gbc);

        gbc.gridy++;
        backgroundPanel.add(exitButton, gbc);

        // Add escape key listener to exit
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        backgroundPanel.registerKeyboardAction(e -> System.exit(0), 
            escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JLabel createTitleLabel() {
        JLabel label = new JLabel("WORDSCAPES") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                super.paintComponent(g2d);
            }
        };
        label.setFont(new Font("SansSerif", Font.BOLD, 72));
        label.setForeground(new Color(30, 30, 50));
        label.setBorder(new EmptyBorder(30, 30, 50, 30));
        label.setOpaque(false);
        return label;
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(250, 70));
        button.setFont(new Font("SansSerif", Font.BOLD, 24));
        button.setFocusPainted(false);
        button.setForeground(new Color(30, 30, 50));
        button.setBackground(new Color(180, 200, 230, 200));
        button.setBorder(new LineBorder(new Color(120, 140, 170, 200), 3, true));
        button.setActionCommand(actionCommand);
        button.addActionListener(this);
        button.setOpaque(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(120, 140, 170, 250));
                button.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(180, 200, 230, 200));
                button.setForeground(new Color(30, 30, 50));
            }
        });

        return button;
    }

    private void startTitleFadeIn() {
        fadeInTimer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.05f;
                if (alpha >= 1f) {
                    alpha = 1f;
                    fadeInTimer.stop();
                }
                titleLabel.repaint();
            }
        });
        fadeInTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "play":
            	SwingUtilities.invokeLater(() -> {
                    Game game = new Game(this);
                    game.setVisible(true);
                    this.dispose();
                });
                break;
            case "settings":
                openSettings();
                break;
            case "exit":
                System.exit(0);
                break;
        }
    }
    private void openSettings() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(3, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JCheckBox soundToggle = new JCheckBox("Sound");
        JCheckBox musicToggle = new JCheckBox("Music");

        settingsPanel.add(soundToggle);
        settingsPanel.add(musicToggle);

        int result = JOptionPane.showConfirmDialog(this, settingsPanel, "Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, "Settings saved!", "Wordscapes", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}