package com.library.dao;

import com.library.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserDAO extends BaseDAO<User> {
    private static final String INSERT_SQL = "INSERT INTO users (username, password, name, email, user_type) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE users SET username = ?, password = ?, name = ?, email = ?, user_type = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM users WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM users";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM users WHERE id = ?";
    private static final String SELECT_BY_USERNAME_SQL = "SELECT * FROM users WHERE username = ?";
    private static final String SELECT_BY_TYPE_SQL = "SELECT * FROM users WHERE user_type = ?";

    public boolean addUser(User user) {
        // Check for duplicate username
        if (getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check for duplicate email
        String checkEmailSQL = "SELECT * FROM users WHERE email = ?";
        List<User> existingUsers = executeQuery(checkEmailSQL, new Object[]{user.getEmail()}, this::mapResultSet);
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("Email already exists");
        }

        Object[] params = {
            user.getUsername(),
            user.getPassword(),
            user.getName(),
            user.getEmail(),
            user.getUserType().toString()
        };
        return executeUpdate(INSERT_SQL, params) > 0;
    }

    public boolean updateUser(User user) {
        // Check for duplicate username (excluding current user)
        User existingUser = getUserByUsername(user.getUsername());
        if (existingUser != null && existingUser.getId() != user.getId()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check for duplicate email (excluding current user)
        String checkEmailSQL = "SELECT * FROM users WHERE email = ? AND id != ?";
        List<User> existingUsers = executeQuery(checkEmailSQL, 
            new Object[]{user.getEmail(), user.getId()}, this::mapResultSet);
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("Email already exists");
        }

        Object[] params = {
            user.getUsername(),
            user.getPassword(),
            user.getName(),
            user.getEmail(),
            user.getUserType().toString(),
            user.getId()
        };
        return executeUpdate(UPDATE_SQL, params) > 0;
    }

    public boolean deleteUser(int userId) {
        return executeUpdate(DELETE_SQL, new Object[]{userId}) > 0;
    }

    public List<User> getAllUsers() {
        return executeQuery(SELECT_ALL_SQL, null, this::mapResultSet);
    }

    public User getUserById(int userId) {
        List<User> users = executeQuery(SELECT_BY_ID_SQL, new Object[]{userId}, this::mapResultSet);
        return users.isEmpty() ? null : users.get(0);
    }

    public User getUserByUsername(String username) {
        List<User> users = executeQuery(SELECT_BY_USERNAME_SQL, new Object[]{username}, this::mapResultSet);
        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> getUsersByType(User.UserType userType) {
        return executeQuery(SELECT_BY_TYPE_SQL, new Object[]{userType.toString()}, this::mapResultSet);
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("name"),
            rs.getString("email"),
            User.UserType.valueOf(rs.getString("user_type"))
        );
    }
} 