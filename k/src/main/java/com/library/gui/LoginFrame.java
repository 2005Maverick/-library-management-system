package com.library.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.library.model.User;
import com.library.db.DatabaseManager;
import java.sql.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;

    public LoginFrame() {
        setTitle("Library Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Create components
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        mainPanel.add(passwordField, gbc);

        // User Type
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1;
        String[] userTypes = {"ADMIN", "LIBRARIAN", "STUDENT"};
        userTypeComboBox = new JComboBox<>(userTypes);
        mainPanel.add(userTypeComboBox, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword()).trim();
                String userType = (String) userTypeComboBox.getSelectedItem();
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Please enter both username and password!",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                User user = authenticate(username, password, userType);
                if (user != null) {
                    openDashboard(user);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Invalid credentials!",
                            "Login Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        mainPanel.add(loginButton, gbc);

        add(mainPanel);
    }

    private User authenticate(String username, String password, String userType) {
        try {
            Connection conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND user_type = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email"),
                    User.UserType.valueOf(rs.getString("user_type"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void openDashboard(User user) {
        BaseDashboard dashboard;
        switch (user.getUserType()) {
            case ADMIN:
                dashboard = new AdminDashboard(user);
                break;
            case LIBRARIAN:
                dashboard = new LibrarianDashboard(user);
                break;
            case STUDENT:
                dashboard = new StudentDashboard(user);
                break;
            default:
                throw new IllegalArgumentException("Invalid user type");
        }
        dashboard.setVisible(true);
    }
} 