package com.library.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.dao.BookDAO;
import com.library.dao.TransactionDAO;
import com.library.model.User;

public class StudentDashboard extends BaseDashboard {
    private JButton requestBookButton;
    private JButton returnBookButton;
    private JTable booksTable;
    private JTable transactionsTable;
    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private JTextField searchField;
    private JComboBox<String> searchType;

    public StudentDashboard(User user) {
        super(user);
        this.bookDAO = new BookDAO();
        this.transactionDAO = new TransactionDAO();
        initializeComponents();
        layoutComponents();
    }

    @Override
    protected void initializeComponents() {
        // Initialize search components
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        searchType = new JComboBox<>(new String[]{"Title", "Author", "ISBN"});
        searchType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchType.setBackground(Color.WHITE);
        
        JButton searchButton = createModernButton("Search", new Color(46, 204, 113));
        searchButton.addActionListener(e -> searchBooks());

        // Initialize action buttons
        requestBookButton = createModernButton("Request Book", new Color(52, 152, 219));
        requestBookButton.addActionListener(e -> requestBook());

        returnBookButton = createModernButton("Return Book", new Color(155, 89, 182));
        returnBookButton.addActionListener(e -> returnBook());

        // Initialize tables with modern design
        booksTable = createModernTable(
            new Object[][]{},
            new String[]{"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"}
        );
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        transactionsTable = createModernTable(
            new Object[][]{},
            new String[]{"ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Status", "Fine"}
        );
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    protected void layoutComponents() {
        // Create search panel with GridBagLayout for proper sizing
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBackground(new Color(240, 240, 240));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Books"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 0;

        JLabel searchLabel = new JLabel("Search by:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchLabel, gbc);

        searchType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        searchPanel.add(searchType, gbc);

        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        searchPanel.add(searchField, gbc);

        JButton searchButton = createModernButton("Search", new Color(46, 204, 113));
        searchButton.addActionListener(e -> searchBooks());
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchPanel.add(searchButton, gbc);

        // Create books panel
        JPanel booksPanel = createModernPanel();
        booksPanel.setLayout(new BorderLayout());
        booksPanel.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        
        JPanel booksButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        booksButtonPanel.setOpaque(false);
        booksButtonPanel.add(requestBookButton);
        booksPanel.add(booksButtonPanel, BorderLayout.SOUTH);

        // Create transactions panel
        JPanel transactionsPanel = createModernPanel();
        transactionsPanel.setLayout(new BorderLayout());
        transactionsPanel.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);
        
        JPanel transactionsButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        transactionsButtonPanel.setOpaque(false);
        transactionsButtonPanel.add(returnBookButton);
        transactionsPanel.add(transactionsButtonPanel, BorderLayout.SOUTH);

        // Create tabbed pane with custom UI for consistent style
        JTabbedPane tabbedPane = new JTabbedPane() {
            @Override
            public void updateUI() {
                super.updateUI();
                setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                    @Override
                    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                        g.setColor(new Color(44, 62, 80));
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paintTabArea(g, tabPlacement, selectedIndex);
                    }
                    @Override
                    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                        Graphics2D g2 = (Graphics2D) g;
                        if (isSelected) {
                            GradientPaint gp = new GradientPaint(x, y, new Color(0, 255, 255), x + w, y + h, new Color(0, 191, 255));
                            g2.setPaint(gp);
                            g2.fillRoundRect(x, y, w, h, 10, 10);
                        } else {
                            g2.setColor(new Color(236, 240, 241));
                            g2.fillRect(x, y, w, h);
                        }
                    }
                    @Override
                    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                        // No border for flat look
                    }
                });
            }
        };
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Search Books", booksPanel);
        tabbedPane.addTab("My Transactions", transactionsPanel);

        // Add components to content panel
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Load initial data
        loadBookData();
        loadTransactionData();
    }

    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadBookData();
            return;
        }

        List<Book> books = bookDAO.searchBooks(searchTerm);
        updateBooksTable(books);
    }

    private void loadBookData() {
        try {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"},
                0
            );
            booksTable.setModel(model);

            List<Book> books = bookDAO.getAllBooks();
            for (Book book : books) {
                model.addRow(new Object[]{
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getIsbn(),
                        book.getAvailableQuantity(),
                        book.getQuantity(),
                        book.getCategory()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading books: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionData() {
        try {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Book Title", "Issue Date", "Due Date", "Return Date", "Status", "Fine"},
                0
            );
            transactionsTable.setModel(model);

            List<Transaction> transactions = transactionDAO.getTransactionsByUser(user.getId());
            for (Transaction transaction : transactions) {
                String status = transaction.getStatus().toString();
                if (transaction.getStatus() == Transaction.TransactionStatus.OVERDUE) {
                    status = "Overdue";
                } else if (transaction.getStatus() == Transaction.TransactionStatus.REQUESTED) {
                    status = "Requested";
                } else if (transaction.getStatus() == Transaction.TransactionStatus.REJECTED) {
                    status = "Rejected";
                }

                model.addRow(new Object[]{
                        transaction.getId(),
                        transaction.getBookTitle(),
                        transaction.getIssueDate(),
                        transaction.getDueDate(),
                        transaction.getReturnDate(),
                        status,
                        String.format("$%.2f", transaction.getFine())
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading transactions: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBooksTable(List<Book> books) {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"},
            0
        );
        booksTable.setModel(model);

        for (Book book : books) {
            model.addRow(new Object[]{
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getAvailableQuantity(),
                book.getQuantity(),
                book.getCategory()
            });
        }
    }

    private void requestBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to request!",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = (int) booksTable.getValueAt(selectedRow, 0);
        try {
            if (transactionDAO.requestBook(bookId, user.getId())) {
                JOptionPane.showMessageDialog(this,
                        "Book request submitted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadBookData();
                loadTransactionData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to request book!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error requesting book: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a transaction to return!",
                    "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
        try {
            if (transactionDAO.returnBook(transactionId)) {
                JOptionPane.showMessageDialog(this,
                        "Book returned successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadBookData();
                loadTransactionData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to return book!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error returning book: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 