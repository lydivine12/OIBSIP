package com.reservation;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Online Reservation System - Login");
        setSize(430, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("ONLINE RESERVATION SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.add(UIUtils.label("Username:"));
        form.add(usernameField);
        form.add(UIUtils.label("Password:"));
        form.add(passwordField);

        JButton loginButton = UIUtils.button("Login");
        JButton exitButton = UIUtils.button("Exit");
        form.add(loginButton);
        form.add(exitButton);

        root.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("Demo: admin / admin123", SwingConstants.CENTER);
        hint.setForeground(Color.DARK_GRAY);
        root.add(hint, BorderLayout.SOUTH);

        setContentPane(root);

        loginButton.addActionListener(e -> login());
        exitButton.addActionListener(e -> System.exit(0));
        passwordField.addActionListener(e -> login());

        UIUtils.center(this);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (Validation.isEmpty(username) || Validation.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Username and password are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userDAO.authenticate(username, password)) {
            dispose();
            new DashboardFrame(username).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.",
                    "Access Denied", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
        }
    }
}
