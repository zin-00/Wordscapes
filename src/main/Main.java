package main;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Menu menu = new Menu(null);
                menu.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}