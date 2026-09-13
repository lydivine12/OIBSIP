package com.reservation;

import javax.swing.*;
import java.awt.*;

public class CancellationFrame extends JFrame {
    private final JTextField pnrField = new JTextField();
    private final JTextArea detailsArea = new JTextArea();

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private Reservation currentReservation;

    public CancellationFrame() {
        setTitle("Cancel Booking");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("CANCELLATION", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.add(UIUtils.label("PNR Number:"), BorderLayout.WEST);
        top.add(pnrField, BorderLayout.CENTER);

        JButton fetchButton = UIUtils.button("Fetch");
        top.add(fetchButton, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBorder(BorderFactory.createTitledBorder("Booking Details"));
        root.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        JButton cancelButton = UIUtils.button("Confirm Cancellation");
        cancelButton.setEnabled(false);
        JButton closeButton = UIUtils.button("Close");

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(cancelButton);
        bottom.add(closeButton);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        fetchButton.addActionListener(e -> fetch());
        pnrField.addActionListener(e -> fetch());
        cancelButton.addActionListener(e -> cancel());
        closeButton.addActionListener(e -> dispose());

        // Keep button state synchronized with the fetched reservation.
        this.cancelButton = cancelButton;

        UIUtils.center(this);
    }

    private final JButton cancelButton;

    private void fetch() {
        String pnr = pnrField.getText().trim();

        if (Validation.isEmpty(pnr)) {
            JOptionPane.showMessageDialog(this, "Enter a PNR number.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentReservation = reservationDAO.findByPNR(pnr);

        if (currentReservation == null) {
            detailsArea.setText("No booking was found for PNR: " + pnr);
            cancelButton.setEnabled(false);
            return;
        }

        Reservation r = currentReservation;
        detailsArea.setText("""
                PNR:                 %s
                Passenger Name:      %s
                Train Number:        %d
                Train Name:          %s
                Class Type:           %s
                Journey Date:         %s
                Source Station:       %s
                Destination Station: %s
                Booked At:            %s
                """.formatted(
                r.getPnr(), r.getPassengerName(), r.getTrainNumber(),
                r.getTrainName(), r.getClassType(), r.getJourneyDate(),
                r.getSourceStation(), r.getDestinationStation(),
                r.getCreatedAt()
        ));
        cancelButton.setEnabled(true);
    }

    private void cancel() {
        if (currentReservation == null) return;

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel booking " +
                        currentReservation.getPnr() + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) return;

        if (reservationDAO.deleteByPNR(currentReservation.getPnr())) {
            JOptionPane.showMessageDialog(this,
                    "Booking " + currentReservation.getPnr() + " has been cancelled.",
                    "Cancellation Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            currentReservation = null;
            detailsArea.setText("");
            pnrField.setText("");
            cancelButton.setEnabled(false);
        }
    }
}
