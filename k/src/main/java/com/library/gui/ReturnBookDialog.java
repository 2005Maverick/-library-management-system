package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.model.User;
import com.library.dao.BookDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.UserDAO;

public class ReturnBookDialog extends JDialog {
    private int transactionId;
    private TransactionDAO transactionDAO;
    private BookDAO bookDAO;
    private UserDAO userDAO;

    private JLabel bookLabel;
    private JLabel userLabel;
    private JLabel issueDateLabel;
    private JLabel dueDateLabel;
    private JLabel fineLabel;
    private JTextField fineField;
    private JButton returnButton;
    private JButton cancelButton;

    public ReturnBookDialog(JFrame parent, int transactionId) {
        super(parent, "Return Book", true);
        this.transactionId = transactionId;
        this.transactionDAO = new TransactionDAO();
        this.bookDAO = new BookDAO();
        this.userDAO = new UserDAO();

        initializeComponents();
        layoutComponents();
        loadTransactionData();
    }

    private void initializeComponents() {
        bookLabel = new JLabel();
        userLabel = new JLabel();
        issueDateLabel = new JLabel();
        dueDateLabel = new JLabel();
        fineLabel = new JLabel("Fine Amount:");
        fineField = new JTextField(10);
        fineField.setEditable(false);

        returnButton = new JButton("Return Book");
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Book information
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(bookLabel, gbc);

        // User information
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("User:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(userLabel, gbc);

        // Issue date
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Issue Date:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(issueDateLabel, gbc);

        // Due date
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(dueDateLabel, gbc);

        // Fine amount
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(fineLabel, gbc);
        gbc.gridx = 1;
        mainPanel.add(fineField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(returnButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void loadTransactionData() {
        Transaction transaction = transactionDAO.getTransactionById(transactionId);
        if (transaction == null) {
            JOptionPane.showMessageDialog(this,
                    "Transaction not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        Book book = bookDAO.getBookById(transaction.getBookId());
        User user = userDAO.getUserById(transaction.getUserId());

        bookLabel.setText(book != null ? book.getTitle() : "Unknown");
        userLabel.setText(user != null ? user.getUsername() : "Unknown");
        issueDateLabel.setText(transaction.getIssueDate().toString());
        dueDateLabel.setText(transaction.getDueDate().toString());

        // Calculate fine if overdue
        Date today = new Date();
        if (today.after(transaction.getDueDate())) {
            long daysOverdue = (today.getTime() - transaction.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
            double fine = daysOverdue * 5.0; // $5 per day fine
            fineField.setText(String.format("$%.2f", fine));
        } else {
            fineField.setText("$0.00");
        }
    }

    private void returnBook() {
        boolean success = transactionDAO.returnBook(transactionId);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Book returned successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to return book!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 