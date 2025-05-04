package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.List;
import com.library.model.Book;
import com.library.model.User;
import com.library.dao.BookDAO;
import com.library.dao.UserDAO;
import com.library.dao.TransactionDAO;

public class IssueBookDialog extends JDialog {
    private JComboBox<Book> bookComboBox;
    private JComboBox<User> userComboBox;
    private JSpinner dueDateSpinner;
    private JButton issueButton;
    private JButton cancelButton;
    private JButton scanQRButton;
    private BookDAO bookDAO;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;

    public IssueBookDialog(JFrame parent) {
        super(parent, "Issue Book", true);
        bookDAO = new BookDAO();
        userDAO = new UserDAO();
        transactionDAO = new TransactionDAO();

        initializeComponents();
        layoutComponents();
        loadData();
    }

    private void initializeComponents() {
        bookComboBox = new JComboBox<>();
        userComboBox = new JComboBox<>();

        // Set up due date spinner (default 14 days from now)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        dueDateSpinner = new JSpinner(new SpinnerDateModel(calendar.getTime(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd");
        dueDateSpinner.setEditor(dateEditor);

        issueButton = new JButton("Issue");
        issueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                issueBook();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // Scan QR button
        scanQRButton = new JButton("Scan QR");
        scanQRButton.addActionListener(e -> onScanQR());
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Book selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(bookComboBox, gbc);

        // User selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("User:"), gbc);
        gbc.gridx = 1;
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(userComboBox, BorderLayout.CENTER);
        userPanel.add(scanQRButton, BorderLayout.EAST);
        mainPanel.add(userPanel, gbc);

        // Due date
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(dueDateSpinner, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(issueButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void loadData() {
        // Load available books
        List<Book> books = bookDAO.getAllBooks();
        for (Book book : books) {
            if (book.getAvailableQuantity() > 0) {
                bookComboBox.addItem(book);
            }
        }

        // Load students
        List<User> users = userDAO.getUsersByType(User.UserType.STUDENT);
        for (User user : users) {
            userComboBox.addItem(user);
        }
    }

    private void issueBook() {
        Book selectedBook = (Book) bookComboBox.getSelectedItem();
        User selectedUser = (User) userComboBox.getSelectedItem();

        if (selectedBook == null || selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select both a book and a user!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Request the book
        boolean success = transactionDAO.requestBook(
                selectedBook.getId(),
                selectedUser.getId());

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Book request submitted successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to submit book request!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onScanQR() {
        QRScannerDialog qrDialog = new QRScannerDialog((JFrame) SwingUtilities.getWindowAncestor(this));
        qrDialog.setVisible(true);
        String scanned = qrDialog.getScannedText();
        if (scanned != null && scanned.startsWith("STUDENT:")) {
            String studentIdStr = scanned.substring("STUDENT:".length()).trim();
            try {
                int studentId = Integer.parseInt(studentIdStr);
                for (int i = 0; i < userComboBox.getItemCount(); i++) {
                    User user = userComboBox.getItemAt(i);
                    if (user.getId() == studentId) {
                        userComboBox.setSelectedIndex(i);
                        // If a book is selected, issue the book
                        if (bookComboBox.getSelectedItem() != null) {
                            issueBook();
                        }
                        return;
                    }
                }
                JOptionPane.showMessageDialog(this, "Student not found in the list!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid student ID in QR code!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (scanned != null) {
            JOptionPane.showMessageDialog(this, "Invalid QR code format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} 