package com.library.gui;

import javax.swing.*;
import java.awt.*;
import com.library.model.User;
import com.library.model.Book;
import com.library.dao.UserDAO;
import com.library.dao.BookDAO;
import java.util.List;

public class AdminDashboard extends BaseDashboard {
    private JTabbedPane tabbedPane;
    private UserDAO userDAO;
    private BookDAO bookDAO;

    // User Management Components
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;
    private JTable usersTable;

    // Book Management Components
    private JButton addBookButton;
    private JButton editBookButton;
    private JButton deleteBookButton;
    private JTable booksTable;

    public AdminDashboard(User user) {
        super(user);
        this.userDAO = new UserDAO();
        this.bookDAO = new BookDAO();
        initializeComponents();
        layoutComponents();
    }

    @Override
    protected void initializeComponents() {
        // Initialize User Management Components
        addUserButton = new JButton("Add User");
        addUserButton.addActionListener(e -> {
            UserDialog dialog = new UserDialog(this, null);
            dialog.setVisible(true);
            loadUserData();
        });

        editUserButton = new JButton("Edit User");
        editUserButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow >= 0) {
                int userId = (int) usersTable.getValueAt(selectedRow, 0);
                User userToEdit = userDAO.getUserById(userId);
                if (userToEdit != null) {
                    UserDialog dialog = new UserDialog(this, userToEdit);
                    dialog.setVisible(true);
                    loadUserData();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a user to edit!",
                        "Selection Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteUserButton = new JButton("Delete User");
        deleteUserButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow >= 0) {
                int userId = (int) usersTable.getValueAt(selectedRow, 0);
                User userToDelete = userDAO.getUserById(userId);
                
                if (userToDelete != null) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete this user?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (userDAO.deleteUser(userId)) {
                            JOptionPane.showMessageDialog(this,
                                    "User deleted successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadUserData();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to delete user!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a user to delete!",
                        "Selection Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Initialize Book Management Components
        addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(e -> {
            BookDialog dialog = new BookDialog(this, null);
            dialog.setVisible(true);
            loadBookData();
        });

        editBookButton = new JButton("Edit Book");
        editBookButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow >= 0) {
                int bookId = (int) booksTable.getValueAt(selectedRow, 0);
                Book bookToEdit = bookDAO.getBookById(bookId);
                if (bookToEdit != null) {
                    BookDialog dialog = new BookDialog(this, bookToEdit);
                    dialog.setVisible(true);
                    loadBookData();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a book to edit!",
                        "Selection Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteBookButton = new JButton("Delete Book");
        deleteBookButton.addActionListener(e -> {
            int selectedRow = booksTable.getSelectedRow();
            if (selectedRow >= 0) {
                int bookId = (int) booksTable.getValueAt(selectedRow, 0);
                Book bookToDelete = bookDAO.getBookById(bookId);
                
                if (bookToDelete != null) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete this book?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        if (bookDAO.deleteBook(bookId)) {
                            JOptionPane.showMessageDialog(this,
                                    "Book deleted successfully!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadBookData();
                        } else {
                            JOptionPane.showMessageDialog(this,
                                    "Failed to delete book!",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Please select a book to delete!",
                        "Selection Error",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        // Initialize Tables
        String[] userColumns = {"ID", "Username", "Name", "Email", "User Type"};
        usersTable = new JTable(new Object[0][userColumns.length], userColumns);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        String[] bookColumns = {"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"};
        booksTable = new JTable(new Object[0][bookColumns.length], bookColumns);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    protected void layoutComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane() {
            @Override
            public void updateUI() {
                super.updateUI();
                setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                    @Override
                    protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
                        g.setColor(new Color(44, 62, 80)); // Match header background
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
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tabbedPane.setForeground(new Color(44, 62, 80));
        tabbedPane.setBackground(new Color(44, 62, 80));
        tabbedPane.setOpaque(false);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Add User Management Tab
        JPanel usersPanel = new JPanel(new BorderLayout());
        JPanel userActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userActionPanel.add(addUserButton);
        userActionPanel.add(editUserButton);
        userActionPanel.add(deleteUserButton);
        usersPanel.add(userActionPanel, BorderLayout.NORTH);
        usersPanel.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        usersPanel.setBorder(BorderFactory.createTitledBorder("Users"));
        tabbedPane.addTab("Users", usersPanel);

        // Add Book Management Tab
        JPanel booksPanel = new JPanel(new BorderLayout());
        JPanel bookActionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookActionPanel.add(addBookButton);
        bookActionPanel.add(editBookButton);
        bookActionPanel.add(deleteBookButton);
        booksPanel.add(bookActionPanel, BorderLayout.NORTH);
        booksPanel.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        booksPanel.setBorder(BorderFactory.createTitledBorder("Books"));
        tabbedPane.addTab("Books", booksPanel);

        // Load initial data
        loadUserData();
        loadBookData();
    }

    private void loadUserData() {
        List<User> users = userDAO.getAllUsers();
        Object[][] data = new Object[users.size()][5];
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            data[i][0] = user.getId();
            data[i][1] = user.getUsername();
            data[i][2] = user.getName();
            data[i][3] = user.getEmail();
            data[i][4] = user.getUserType().toString();
        }

        usersTable.setModel(new javax.swing.table.DefaultTableModel(
            data,
            new String[]{"ID", "Username", "Name", "Email", "User Type"}
        ));
    }

    private void loadBookData() {
        List<Book> books = bookDAO.getAllBooks();
        Object[][] data = new Object[books.size()][7];
        
        for (int i = 0; i < books.size(); i++) {
            Book book = books.get(i);
            data[i][0] = book.getId();
            data[i][1] = book.getTitle();
            data[i][2] = book.getAuthor();
            data[i][3] = book.getIsbn();
            data[i][4] = book.getAvailableQuantity();
            data[i][5] = book.getQuantity();
            data[i][6] = book.getCategory();
        }

        booksTable.setModel(new javax.swing.table.DefaultTableModel(
            data,
            new String[]{"ID", "Title", "Author", "ISBN", "Available", "Total", "Category"}
        ));
    }
} 