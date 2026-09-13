package com.reservation;

import javax.swing.*;
import java.awt.*;

public class UIUtils {
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        return l;
    }

    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        return b;
    }

    public static void center(JFrame frame) {
        frame.setLocationRelativeTo(null);
    }

    private UIUtils() {}
}
