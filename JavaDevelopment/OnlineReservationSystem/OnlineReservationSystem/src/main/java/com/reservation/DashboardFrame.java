package com.reservation;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    public DashboardFrame(String username) {
        setTitle("Online Reservation System - Dashboard");
        setSize(550, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(20, 20));
        root.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel welcome = new JLabel("Welcome, " + username + "!", SwingConstants.CENTER);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));
        root.add(welcome, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new GridLayout(3, 1, 15, 15));
        JButton reserve = UIUtils.button("Book / Reserve Ticket");
        JButton cancel = UIUtils.button("Cancel Booking");
        JButton logout = UIUtils.button("Logout");

        buttons.add(reserve);
        buttons.add(cancel);
        buttons.add(logout);
        root.add(buttons, BorderLayout.CENTER);

        JLabel info = new JLabel("Choose an operation", SwingConstants.CENTER);
        root.add(info, BorderLayout.SOUTH);

        setContentPane(root);

        reserve.addActionListener(e -> new ReservationFrame().setVisible(true));
        cancel.addActionListener(e -> new CancellationFrame().setVisible(true));
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        UIUtils.center(this);
    }
}
