package com.library.dao;

import com.library.db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    protected Connection getConnection() throws SQLException {
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn == null || conn.isClosed()) {
            throw new SQLException("Database connection is not available");
        }
        return conn;
    }

    protected List<T> executeQuery(String sql, Object[] params, ResultSetMapper<T> mapper) {
        List<T> results = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        pstmt.setObject(i + 1, params[i]);
                    }
                }
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database query error: " + e.getMessage(), e);
        }
        return results;
    }

    protected int executeUpdate(String sql, Object[] params) {
        Connection conn = null;
        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                if (params != null) {
                    for (int i = 0; i < params.length; i++) {
                        pstmt.setObject(i + 1, params[i]);
                    }
                }
                return pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database update error: " + e.getMessage(), e);
        }
    }

    protected void executeBatch(List<String> sqlStatements, List<Object[]> paramsList) {
        if (sqlStatements.size() != paramsList.size()) {
            throw new IllegalArgumentException("Number of SQL statements must match number of parameter arrays");
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            try {
                for (int i = 0; i < sqlStatements.size(); i++) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlStatements.get(i))) {
                        Object[] params = paramsList.get(i);
                        if (params != null) {
                            for (int j = 0; j < params.length; j++) {
                                pstmt.setObject(j + 1, params[j]);
                            }
                        }
                        pstmt.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Batch execution failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database batch error: " + e.getMessage(), e);
        }
    }

    protected interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
} 