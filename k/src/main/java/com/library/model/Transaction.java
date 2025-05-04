package com.library.model;

import java.util.Date;

public class Transaction {
    private int id;
    private int bookId;
    private int userId;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private double fine;
    private TransactionStatus status;
    private String bookTitle;
    private String userName;

    public enum TransactionStatus {
        REQUESTED,
        ISSUED,
        RETURNED,
        OVERDUE,
        REJECTED
    }

    public Transaction() {
        // Default constructor
    }

    public Transaction(int id, int bookId, int userId, Date issueDate, Date dueDate, 
            Date returnDate, double fine, TransactionStatus status) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fine = fine;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public double getFine() {
        return fine;
    }

    public void setFine(double fine) {
        this.fine = fine;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double calculateFine() {
        if (status != TransactionStatus.OVERDUE || returnDate == null) {
            return 0.0;
        }
        
        long daysOverdue = (returnDate.getTime() - dueDate.getTime()) / (1000 * 60 * 60 * 24);
        if (daysOverdue > 0) {
            return daysOverdue * 5.0; // $5 per day fine
        }
        return 0.0;
    }
} 