package main;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BootstrapSwing {

    public static final Color PRIMARY_COLOR = new Color(0, 123, 255);
    public static final Color SECONDARY_COLOR = new Color(108, 117, 125);
    public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    public static final Color DANGER_COLOR = new Color(220, 53, 69);
    public static final Color WARNING_COLOR = new Color(255, 193, 7);
    public static final Color INFO_COLOR = new Color(23, 162, 184);
    public static final Color LIGHT_COLOR = new Color(248, 249, 250);
    public static final Color DARK_COLOR = new Color(52, 58, 64);

    public static class BootstrapButton extends JButton {
        public BootstrapButton(String text, Color bgColor, Color fgColor) {
            super(text);
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setBackground(bgColor);
            setForeground(fgColor);
            setFont(new Font("Arial", Font.BOLD, 14));
            setBorder(new EmptyBorder(10, 15, 10, 15));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(bgColor.darker());
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(bgColor);
                }
            });
        }
    }

    public static class BootstrapPanel extends JPanel {
        public BootstrapPanel() {
            setBackground(LIGHT_COLOR);
            setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230), 1));
            setLayout(new BorderLayout(10, 10));
        }
    }

    public static class BootstrapLabel extends JLabel {
        public BootstrapLabel(String text, Color color) {
            super(text);
            setFont(new Font("Arial", Font.PLAIN, 14));
            setForeground(color);
        }
    }

    public static class BootstrapTextField extends JTextField {
        public BootstrapTextField() {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            setFont(new Font("Arial", Font.PLAIN, 14));
        }
    }
}

