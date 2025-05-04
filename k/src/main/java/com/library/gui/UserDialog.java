package com.library.gui;

import com.library.model.User;
import com.library.dao.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class UserDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField emailField;
    private JComboBox<User.UserType> userTypeComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private User user;
    private boolean isEditMode;
    private UserDAO userDAO;

    public UserDialog(JFrame parent, User user) {
        super(parent, user == null ? "Add User" : "Edit User", true);
        this.user = user;
        this.isEditMode = user != null;
        this.userDAO = new UserDAO();
        initializeComponents();
        layoutComponents();
        if (isEditMode) {
            populateFields();
        }
    }

    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        nameField = new JTextField(20);
        emailField = new JTextField(20);

        // User Type ComboBox
        userTypeComboBox = new JComboBox<>(User.UserType.values());
        userTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // If changing to student, make email optional
                boolean isStudent = e.getItem() == User.UserType.STUDENT;
                emailField.setBackground(isStudent ? Color.WHITE : Color.PINK);
            }
        });

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveUser());

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(emailField, gbc);

        // User Type
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("User Type:"), gbc);
        gbc.gridx = 1;
        mainPanel.add(userTypeComboBox, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(getParent());
    }

    private void populateFields() {
        usernameField.setText(user.getUsername());
        nameField.setText(user.getName());
        emailField.setText(user.getEmail());
        userTypeComboBox.setSelectedItem(user.getUserType());
        
        // Don't populate password field for security
        passwordField.setText("");
    }

    private void saveUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        User.UserType userType = (User.UserType) userTypeComboBox.getSelectedItem();

        // Validation
        if (username.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username and name are required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isEditMode && password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Password is required for new users!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (userType != User.UserType.STUDENT && email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Email is required for staff members!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if username already exists (for new users)
        if (!isEditMode && userDAO.getUserByUsername(username) != null) {
            JOptionPane.showMessageDialog(this,
                    "Username already exists!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isEditMode) {
            // Update existing user
            user.setUsername(username);
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setName(name);
            user.setEmail(email);
            user.setUserType(userType);
            userDAO.updateUser(user);
        } else {
            // Create new user
            user = new User(0, username, password, name, email, userType);
            userDAO.addUser(user);
        }

        dispose();
    }
} 