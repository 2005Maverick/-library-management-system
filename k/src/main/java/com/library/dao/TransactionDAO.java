package com.library.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import com.library.model.Transaction;
import com.library.model.Transaction.TransactionStatus;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.PreparedStatement;

public class TransactionDAO extends BaseDAO<Transaction> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final String REQUEST_BOOK_SQL = "INSERT INTO transactions (book_id, user_id, issue_date, status) VALUES (?, ?, ?, ?)";
    private static final String APPROVE_REQUEST_SQL = "UPDATE transactions SET issue_date = ?, due_date = ?, status = ? WHERE id = ?";
    private static final String REJECT_REQUEST_SQL = "UPDATE transactions SET status = ? WHERE id = ?";
    private static final String RETURN_BOOK_SQL = "UPDATE transactions SET return_date = ?, fine = ?, status = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "ORDER BY t.issue_date DESC";
    private static final String SELECT_BY_ID_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.id = ?";
    private static final String SELECT_BY_USER_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.user_id = ? " +
            "ORDER BY t.issue_date DESC";
    private static final String SELECT_OVERDUE_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.due_date < ? AND t.status = 'ISSUED' " +
            "ORDER BY t.due_date ASC";
    private static final String SELECT_ACTIVE_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.status = 'ISSUED' " +
            "ORDER BY t.due_date ASC";
    private static final String SELECT_REQUESTS_SQL = "SELECT t.*, b.title as book_title, u.username as user_name " +
            "FROM transactions t " +
            "LEFT JOIN books b ON t.book_id = b.id " +
            "LEFT JOIN users u ON t.user_id = u.id " +
            "WHERE t.status = 'REQUESTED' " +
            "ORDER BY t.id DESC";

    public boolean requestBook(int bookId, int userId) {
        Connection conn = null;
        PreparedStatement checkBookStmt = null;
        PreparedStatement checkOverdueStmt = null;
        PreparedStatement requestStmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Check book availability
            String checkBookSQL = "SELECT available_quantity FROM books WHERE id = ? AND available_quantity > 0";
            checkBookStmt = conn.prepareStatement(checkBookSQL);
            checkBookStmt.setInt(1, bookId);
            rs = checkBookStmt.executeQuery();
            
            if (!rs.next()) {
                throw new RuntimeException("Book is not available or does not exist");
            }

            // Check overdue books
            String checkOverdueSQL = "SELECT COUNT(*) as count FROM transactions " +
                                   "WHERE user_id = ? AND status = 'ISSUED' AND due_date < CURRENT_DATE";
            checkOverdueStmt = conn.prepareStatement(checkOverdueSQL);
            checkOverdueStmt.setInt(1, userId);
            rs = checkOverdueStmt.executeQuery();
            
            if (rs.next() && rs.getInt("count") > 0) {
                throw new RuntimeException("You have overdue books. Please return them before requesting new ones.");
            }

            // Create request using the constant
            requestStmt = conn.prepareStatement(REQUEST_BOOK_SQL);
            requestStmt.setInt(1, bookId);
            requestStmt.setInt(2, userId);
            requestStmt.setString(3, null); // issue_date is null for requests
            requestStmt.setString(4, TransactionStatus.REQUESTED.toString());
            
            int rowsAffected = requestStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                conn.commit();
                return true;
            }
            
            conn.rollback();
            return false;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Database error while requesting book: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkBookStmt != null) checkBookStmt.close();
                if (checkOverdueStmt != null) checkOverdueStmt.close();
                if (requestStmt != null) requestStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean approveRequest(int transactionId) {
        Connection conn = null;
        PreparedStatement checkTransactionStmt = null;
        PreparedStatement checkBookStmt = null;
        PreparedStatement updateTransactionStmt = null;
        PreparedStatement updateBookStmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Check transaction status
            String checkTransactionSQL = "SELECT book_id, status FROM transactions WHERE id = ?";
            checkTransactionStmt = conn.prepareStatement(checkTransactionSQL);
            checkTransactionStmt.setInt(1, transactionId);
            rs = checkTransactionStmt.executeQuery();
            
            if (!rs.next()) {
                throw new RuntimeException("Transaction not found");
            }
            
            if (!TransactionStatus.REQUESTED.toString().equals(rs.getString("status"))) {
                throw new RuntimeException("Transaction is not in REQUESTED state");
            }
            
            int bookId = rs.getInt("book_id");

            // Check book availability
            String checkBookSQL = "SELECT available_quantity FROM books WHERE id = ? AND available_quantity > 0";
            checkBookStmt = conn.prepareStatement(checkBookSQL);
            checkBookStmt.setInt(1, bookId);
            rs = checkBookStmt.executeQuery();
            
            if (!rs.next()) {
                throw new RuntimeException("Book is no longer available");
            }

            // Calculate dates
            Calendar cal = Calendar.getInstance();
            Date issueDate = cal.getTime();
            cal.add(Calendar.DAY_OF_MONTH, 14);
            Date dueDate = cal.getTime();

            // Update transaction using constant
            updateTransactionStmt = conn.prepareStatement(APPROVE_REQUEST_SQL);
            updateTransactionStmt.setString(1, DATE_FORMAT.format(issueDate));
            updateTransactionStmt.setString(2, DATE_FORMAT.format(dueDate));
            updateTransactionStmt.setString(3, TransactionStatus.ISSUED.toString());
            updateTransactionStmt.setInt(4, transactionId);
            
            int rowsAffected = updateTransactionStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                String updateBookSQL = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";
                updateBookStmt = conn.prepareStatement(updateBookSQL);
                updateBookStmt.setInt(1, bookId);
                
                if (updateBookStmt.executeUpdate() > 0) {
                    conn.commit();
                    return true;
                }
            }
            
            conn.rollback();
            return false;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Database error while approving request: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkTransactionStmt != null) checkTransactionStmt.close();
                if (checkBookStmt != null) checkBookStmt.close();
                if (updateTransactionStmt != null) updateTransactionStmt.close();
                if (updateBookStmt != null) updateBookStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean rejectRequest(int transactionId) {
        return executeUpdate(REJECT_REQUEST_SQL,
                new Object[]{
                    TransactionStatus.REJECTED.toString(),
                    transactionId
                }) > 0;
    }

    public boolean returnBook(int transactionId) {
        try {
            // Start transaction
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            
            // Get the transaction
            Transaction transaction = getTransactionById(transactionId);
            if (transaction == null) {
                throw new RuntimeException("Transaction not found");
            }
            
            if (transaction.getStatus() != TransactionStatus.ISSUED) {
                throw new RuntimeException("Book is not issued");
            }
            
            // Calculate fine if overdue
            Date returnDate = new Date();
            double fine = 0.0;
            if (returnDate.after(transaction.getDueDate())) {
                long daysOverdue = (returnDate.getTime() - transaction.getDueDate().getTime()) / (1000 * 60 * 60 * 24);
                fine = daysOverdue * 5.0; // $5 per day fine
            }
            
            // Update transaction
            boolean success = executeUpdate(RETURN_BOOK_SQL,
                    new Object[]{
                        DATE_FORMAT.format(returnDate),
                        fine,
                        TransactionStatus.RETURNED.toString(),
                        transactionId
                    }) > 0;
            
            if (success) {
                // Update available quantity
                String updateQuantitySQL = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
                executeUpdate(updateQuantitySQL, new Object[]{transaction.getBookId()});
                conn.commit();
                return true;
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                getConnection().rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Failed to return book: " + e.getMessage());
        }
    }

    public List<Transaction> getAllTransactions() {
        return executeQuery(SELECT_ALL_SQL, new Object[]{}, this::mapResultSet);
    }

    public Transaction getTransactionById(int transactionId) {
        List<Transaction> transactions = executeQuery(SELECT_BY_ID_SQL, new Object[]{transactionId}, this::mapResultSet);
        return transactions.isEmpty() ? null : transactions.get(0);
    }

    public List<Transaction> getTransactionsByUser(int userId) {
        return executeQuery(SELECT_BY_USER_SQL, new Object[]{userId}, this::mapResultSet);
    }

    public List<Transaction> getOverdueTransactions() {
        return executeQuery(SELECT_OVERDUE_SQL, new Object[]{DATE_FORMAT.format(new Date())}, this::mapResultSet);
    }

    public List<Transaction> getActiveTransactions() {
        return executeQuery(SELECT_ACTIVE_SQL, new Object[]{}, this::mapResultSet);
    }

    public List<Transaction> getPendingRequests() {
        return executeQuery(SELECT_REQUESTS_SQL, new Object[]{}, this::mapResultSet);
    }

    private Transaction mapResultSet(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getInt("id"));
        transaction.setBookId(rs.getInt("book_id"));
        transaction.setUserId(rs.getInt("user_id"));
        
        // Handle dates using DATE_FORMAT
        String issueDateStr = rs.getString("issue_date");
        if (issueDateStr != null) {
            try {
                transaction.setIssueDate(DATE_FORMAT.parse(issueDateStr));
            } catch (Exception e) {
                transaction.setIssueDate(null);
            }
        }
        
        String dueDateStr = rs.getString("due_date");
        if (dueDateStr != null) {
            try {
                transaction.setDueDate(DATE_FORMAT.parse(dueDateStr));
            } catch (Exception e) {
                transaction.setDueDate(null);
            }
        }
        
        String returnDateStr = rs.getString("return_date");
        if (returnDateStr != null) {
            try {
                transaction.setReturnDate(DATE_FORMAT.parse(returnDateStr));
            } catch (Exception e) {
                transaction.setReturnDate(null);
            }
        }
        
        transaction.setFine(rs.getDouble("fine"));
        
        // Handle null status
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            transaction.setStatus(TransactionStatus.valueOf(statusStr));
        } else {
            transaction.setStatus(TransactionStatus.REQUESTED);
        }
        
        // Set joined fields
        transaction.setBookTitle(rs.getString("book_title"));
        transaction.setUserName(rs.getString("user_name"));
        
        return transaction;
    }
} 