package com.library.gui;

import javax.swing.*;
import java.awt.*;

public class SettingsDialog extends JDialog {
    private JCheckBox darkModeCheckBox;
    private JButton helpButton;
    private JButton scanQRButton;

    public SettingsDialog(JFrame parent, boolean darkMode, Runnable onToggleDarkMode) {
        super(parent, "Settings", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 10, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Dark Mode toggle
        darkModeCheckBox = new JCheckBox("Dark Mode");
        darkModeCheckBox.setSelected(darkMode);
        darkModeCheckBox.addActionListener(e -> onToggleDarkMode.run());
        add(darkModeCheckBox, gbc);

        // Help button
        gbc.gridy++;
        helpButton = new JButton("Help");
        helpButton.addActionListener(e -> showHelpDialog());
        add(helpButton, gbc);

        // Scan QR button
        gbc.gridy++;
        scanQRButton = new JButton("Scan QR");
        scanQRButton.addActionListener(e -> onScanQR());
        add(scanQRButton, gbc);

        setSize(350, 180);
        setLocationRelativeTo(parent);
    }

    private void showHelpDialog() {
        String helpText = "<html><b>Frequently Asked Questions</b><br><br>"
            + "<b>1. How do I switch between light and dark mode?</b><br>"
            + "Use the moon icon at the bottom of the sidebar or the Dark Mode option in Settings.<br><br>"
            + "<b>2. How do I log out?</b><br>"
            + "Click the Logout button in the top right corner of the header.<br><br>"
            + "<b>3. How do I access settings?</b><br>"
            + "Click the Settings button in the top right corner of the header.<br><br>"
            + "<b>4. How do I search for books?</b><br>"
            + "Use the search bar at the top of the main panel and select the search type.<br><br>"
            + "<b>5. How do I request a book?</b><br>"
            + "Select a book in the table and click the Request Book button.<br><br>"
            + "<b>6. How do I return a book?</b><br>"
            + "Select a transaction in the My Transactions tab and click Return Book.<br><br>"
            + "<b>7. How do I manage users or books (admin only)?</b><br>"
            + "Use the Users or Books tabs in the Admin Dashboard to add, edit, or delete entries.<br><br>"
            + "<b>8. How do I approve or reject book requests (librarian only)?</b><br>"
            + "Go to the Requests tab and use the Approve or Reject buttons.<br><br>"
            + "<b>9. How do I see overdue books or fines?</b><br>"
            + "Check the My Transactions tab for overdue status and fines.<br><br>"
            + "<b>10. How do I get further help?</b><br>"
            + "Contact your system administrator or refer to the user manual.<br></html>";
        JOptionPane.showMessageDialog(this, helpText, "Help - Q&A", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onScanQR() {
        QRScannerDialog qrDialog = new QRScannerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        qrDialog.setVisible(true);
        String scanned = qrDialog.getScannedText();
        if (scanned != null) {
            String type = scanned.startsWith("STUDENT:") ? "Student ID" : scanned.startsWith("BOOK:") ? "Book ID" : "Unknown";
            String id = scanned.contains(":") ? scanned.substring(scanned.indexOf(":") + 1).trim() : scanned;
            JOptionPane.showMessageDialog(this, "Scanned " + type + ": " + id, "QR Scan Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No QR code detected.", "QR Scan", JOptionPane.WARNING_MESSAGE);
        }
    }
} 