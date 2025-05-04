package com.library.gui;

import javax.swing.*;
import java.awt.*;
import com.library.model.Book;
import com.library.dao.BookDAO;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookDialog extends JDialog {
    private Book book;
    private BookDAO bookDAO;
    private boolean isEditMode;

    private JTextField titleField;
    private JTextField authorField;
    private JTextField isbnField;
    private JSpinner quantitySpinner;
    private JTextField categoryField;
    private JButton saveButton;
    private JButton cancelButton;

    public BookDialog(JFrame parent, Book book) {
        super(parent, book == null ? "Add Book" : "Edit Book", true);
        this.book = book;
        this.bookDAO = new BookDAO();
        this.isEditMode = book != null;

        initializeComponents();
        layoutComponents();
        if (isEditMode) {
            loadBookData();
        }
    }

    private void initializeComponents() {
        titleField = new JTextField(20);
        authorField = new JTextField(20);
        isbnField = new JTextField(20);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        categoryField = new JTextField(20);

        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBook();
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

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(titleField, gbc);

        // Author
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(authorField, gbc);

        // ISBN
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(isbnField, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(quantitySpinner, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(categoryField, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        add(mainPanel);
        pack();
        setLocationRelativeTo(getParent());
    }

    private void loadBookData() {
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            isbnField.setText(book.getIsbn());
            quantitySpinner.setValue(book.getQuantity());
            categoryField.setText(book.getCategory());
        }
    }

    private void saveBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        int quantity = (Integer) quantitySpinner.getValue();
        String category = categoryField.getText().trim();

        // Validate input
        if (title.isEmpty() || author.isEmpty() || isbn.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if ISBN already exists (for new books)
        if (!isEditMode) {
            Book existingBook = bookDAO.getBookByISBN(isbn);
            if (existingBook != null) {
                JOptionPane.showMessageDialog(this,
                        "A book with this ISBN already exists!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Create or update book
        Book bookToSave = isEditMode ? book : new Book();
        bookToSave.setTitle(title);
        bookToSave.setAuthor(author);
        bookToSave.setIsbn(isbn);
        bookToSave.setQuantity(quantity);
        bookToSave.setAvailableQuantity(isEditMode ? book.getAvailableQuantity() : quantity);
        bookToSave.setCategory(category);

        boolean success;
        if (isEditMode) {
            success = bookDAO.updateBook(bookToSave);
        } else {
            success = bookDAO.addBook(bookToSave);
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Book " + (isEditMode ? "updated" : "added") + " successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to " + (isEditMode ? "update" : "add") + " book!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 