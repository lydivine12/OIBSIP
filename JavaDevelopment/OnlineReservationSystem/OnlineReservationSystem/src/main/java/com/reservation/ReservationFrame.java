package com.reservation;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReservationFrame extends JFrame {
    private final JTextField passengerField = new JTextField();
    private final JTextField trainNumberField = new JTextField();
    private final JTextField trainNameField = new JTextField();
    private final JComboBox<String> classBox =
            new JComboBox<>(new String[]{"Economy", "Business", "First Class"});
    private final JTextField dateField = new JTextField();
    private final JTextField sourceField = new JTextField();
    private final JTextField destinationField = new JTextField();

    private final TrainDAO trainDAO = new TrainDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    public ReservationFrame() {
        setTitle("Book Ticket");
        setSize(600, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("TICKET RESERVATION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        trainNameField.setEditable(false);
        trainNameField.setBackground(Color.LIGHT_GRAY);
        dateField.setToolTipText("Format: YYYY-MM-DD");

        JPanel form = new JPanel(new GridLayout(8, 2, 10, 10));
        form.add(UIUtils.label("Passenger Name *"));
        form.add(passengerField);
        form.add(UIUtils.label("Train Number *"));
        form.add(trainNumberField);
        form.add(UIUtils.label("Train Name"));
        form.add(trainNameField);
        form.add(UIUtils.label("Class Type *"));
        form.add(classBox);
        form.add(UIUtils.label("Journey Date *"));
        form.add(dateField);
        form.add(UIUtils.label("Source Station *"));
        form.add(sourceField);
        form.add(UIUtils.label("Destination Station *"));
        form.add(destinationField);

        JButton bookButton = UIUtils.button("Insert / Book");
        JButton clearButton = UIUtils.button("Clear");
        form.add(bookButton);
        form.add(clearButton);

        root.add(form, BorderLayout.CENTER);

        JLabel hint = new JLabel("Sample trains: 101, 202, 303, 404, 505");
        root.add(hint, BorderLayout.SOUTH);

        setContentPane(root);

        trainNumberField.addActionListener(e -> lookupTrain());
        trainNumberField.getDocument().addDocumentListener(new SimpleDocumentListener(this::lookupTrain));
        bookButton.addActionListener(e -> book());
        clearButton.addActionListener(e -> clearForm());

        UIUtils.center(this);
    }

    private void lookupTrain() {
        String value = trainNumberField.getText().trim();
        if (value.isEmpty() || !Validation.isNumeric(value)) {
            trainNameField.setText("");
            return;
        }

        Train train = trainDAO.findByNumber(Integer.parseInt(value));
        trainNameField.setText(train == null ? "" : train.getName());
    }

    private void book() {
        String passenger = passengerField.getText().trim();
        String trainNumberText = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String date = dateField.getText().trim();
        String source = sourceField.getText().trim();
        String destination = destinationField.getText().trim();
        String classType = (String) classBox.getSelectedItem();

        if (Validation.isEmpty(passenger) ||
            Validation.isEmpty(trainNumberText) ||
            Validation.isEmpty(date) ||
            Validation.isEmpty(source) ||
            Validation.isEmpty(destination)) {
            showError("Please fill in all required fields.");
            return;
        }

        if (!Validation.isNumeric(trainNumberText)) {
            showError("Train number must be numeric.");
            return;
        }

        if (!Validation.isValidDate(date)) {
            showError("Invalid date. Use YYYY-MM-DD and choose today or a future date.");
            return;
        }

        int trainNumber = Integer.parseInt(trainNumberText);
        Train train = trainDAO.findByNumber(trainNumber);

        if (train == null) {
            showError("Train number does not exist.");
            trainNameField.setText("");
            return;
        }

        if (source.equalsIgnoreCase(destination)) {
            showError("Source and destination stations cannot be the same.");
            return;
        }

        String pnr;
        do {
            pnr = PNRGenerator.generate();
        } while (reservationDAO.findByPNR(pnr) != null);

        String createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Reservation reservation = new Reservation(
                pnr, passenger, trainNumber, train.getName(), classType,
                date, source, destination, createdAt
        );

        if (reservationDAO.insert(reservation)) {
            String details = """
                    BOOKING SUCCESSFUL!

                    PNR: %s
                    Passenger: %s
                    Train: %d - %s
                    Class: %s
                    Journey Date: %s
                    From: %s
                    To: %s
                    """.formatted(
                    pnr, passenger, trainNumber, train.getName(), classType,
                    date, source, destination
            );

            JOptionPane.showMessageDialog(this, details,
                    "Booking Confirmation", JOptionPane.INFORMATION_MESSAGE);
            clearForm();
        }
    }

    private void clearForm() {
        passengerField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        classBox.setSelectedIndex(0);
        dateField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        passengerField.requestFocus();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
