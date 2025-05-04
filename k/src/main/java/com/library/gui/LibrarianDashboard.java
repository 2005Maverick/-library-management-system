package com.library.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.model.User;
import com.library.dao.BookDAO;
import com.library.dao.TransactionDAO;

public class LibrarianDashboard extends BaseDashboard {
    private JTable booksTable;
    private JTable transactionsTable;
    private JTable requestsTable;
    private BookDAO bookDAO;
    private TransactionDAO transactionDAO;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton returnButton;

    public LibrarianDashboard(User user) {
        super(user);
        bookDAO = new BookDAO();
        transactionDAO = new TransactionDAO();
        initializeComponents();
        layoutComponents();
    }

    @Override
    protected void initializeComponents() {
        // Initialize tables with modern design
        booksTable = createModernTable(
            new Object[][]{},
            new String[]{"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"}
        );
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        transactionsTable = createModernTable(
            new Object[][]{},
            new String[]{"ID", "Book Title", "User", "Issue Date", "Due Date", "Return Date", "Status", "Fine"}
        );
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        requestsTable = createModernTable(
            new Object[][]{},
            new String[]{"ID", "Book Title", "User", "Request Date", "Status"}
        );
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Initialize action buttons
        approveButton = createModernButton("Approve", new Color(46, 204, 113));
        approveButton.addActionListener(e -> {
            int selectedRow = requestsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a request to approve!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int transactionId = (int) requestsTable.getValueAt(selectedRow, 0);
            try {
                if (transactionDAO.approveRequest(transactionId)) {
                    JOptionPane.showMessageDialog(this,
                            "Request approved successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRequestData();
                    loadTransactionData();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to approve request!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error approving request: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        rejectButton = createModernButton("Reject", new Color(231, 76, 60));
        rejectButton.addActionListener(e -> {
            int selectedRow = requestsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a request to reject!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int transactionId = (int) requestsTable.getValueAt(selectedRow, 0);
            try {
                if (transactionDAO.rejectRequest(transactionId)) {
                    JOptionPane.showMessageDialog(this,
                            "Request rejected successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRequestData();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to reject request!",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error rejecting request: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        returnButton = createModernButton("Return Book", new Color(155, 89, 182));
        returnButton.addActionListener(e -> {
            int selectedRow = transactionsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a transaction to return!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int transactionId = (int) transactionsTable.getValueAt(selectedRow, 0);
            try {
                if (transactionDAO.returnBook(transactionId)) {
                    JOptionPane.showMessageDialog(this,
                            "Book returned successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadTransactionData();
                    loadBookData();
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
        });
    }

    @Override
    protected void layoutComponents() {
        // Create books panel
        JPanel booksPanel = createModernPanel();
        booksPanel.setLayout(new BorderLayout());
        booksPanel.add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // Create requests panel
        JPanel requestsPanel = createModernPanel();
        requestsPanel.setLayout(new BorderLayout());
        requestsPanel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);
        
        JPanel requestButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        requestButtonPanel.setOpaque(false);
        requestButtonPanel.add(approveButton);
        requestButtonPanel.add(rejectButton);
        requestsPanel.add(requestButtonPanel, BorderLayout.SOUTH);

        // Create transactions panel
        JPanel transactionsPanel = createModernPanel();
        transactionsPanel.setLayout(new BorderLayout());
        transactionsPanel.add(new JScrollPane(transactionsTable), BorderLayout.CENTER);
        
        JPanel transactionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        transactionButtonPanel.setOpaque(false);
        transactionButtonPanel.add(returnButton);
        transactionsPanel.add(transactionButtonPanel, BorderLayout.SOUTH);

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
        tabbedPane.addTab("Books", booksPanel);
        tabbedPane.addTab("Requests", requestsPanel);
        tabbedPane.addTab("Transactions", transactionsPanel);

        // Add components to content panel
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // Load initial data
        loadBookData();
        loadRequestData();
        loadTransactionData();
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

    private void loadRequestData() {
        try {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Book Title", "User", "Request Date", "Status"},
                0
            );
            requestsTable.setModel(model);

            List<Transaction> requests = transactionDAO.getPendingRequests();
            for (Transaction request : requests) {
                model.addRow(new Object[]{
                        request.getId(),
                        request.getBookTitle(),
                        request.getUserName(),
                        request.getIssueDate(),
                        request.getStatus()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading requests: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTransactionData() {
        try {
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Book Title", "User", "Issue Date", "Due Date", "Return Date", "Status", "Fine"},
                0
            );
            transactionsTable.setModel(model);

            List<Transaction> transactions = transactionDAO.getAllTransactions();
            for (Transaction transaction : transactions) {
                model.addRow(new Object[]{
                        transaction.getId(),
                        transaction.getBookTitle(),
                        transaction.getUserName(),
                        transaction.getIssueDate(),
                        transaction.getDueDate(),
                        transaction.getReturnDate(),
                        transaction.getStatus(),
                        transaction.getFine()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading transactions: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} 