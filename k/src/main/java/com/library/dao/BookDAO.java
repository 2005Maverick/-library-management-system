package com.library.dao;

import com.library.model.Book;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class BookDAO extends BaseDAO<Book> {
    private static final String INSERT_SQL = "INSERT INTO books (title, author, isbn, publisher, year, quantity, available_quantity, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE books SET title = ?, author = ?, isbn = ?, publisher = ?, year = ?, quantity = ?, available_quantity = ?, category = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM books WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM books";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM books WHERE id = ?";
    private static final String SEARCH_SQL = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
    private static final String UPDATE_AVAILABLE_QUANTITY_SQL = "UPDATE books SET available_quantity = ? WHERE id = ?";

    public boolean addBook(Book book) {
        // Check for duplicate ISBN
        if (getBookByISBN(book.getIsbn()) != null) {
            throw new RuntimeException("A book with this ISBN already exists");
        }

        Object[] params = {
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPublisher(),
            book.getYear(),
            book.getQuantity(),
            book.getQuantity(), // Set available_quantity same as quantity for new books
            book.getCategory()
        };
        return executeUpdate(INSERT_SQL, params) > 0;
    }

    public boolean updateBook(Book book) {
        // Check for duplicate ISBN (excluding current book)
        Book existingBook = getBookByISBN(book.getIsbn());
        if (existingBook != null && existingBook.getId() != book.getId()) {
            throw new RuntimeException("A book with this ISBN already exists");
        }

        // Ensure available_quantity doesn't exceed total quantity
        if (book.getAvailableQuantity() > book.getQuantity()) {
            throw new RuntimeException("Available quantity cannot exceed total quantity");
        }

        Object[] params = {
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPublisher(),
            book.getYear(),
            book.getQuantity(),
            book.getAvailableQuantity(),
            book.getCategory(),
            book.getId()
        };
        return executeUpdate(UPDATE_SQL, params) > 0;
    }

    public boolean deleteBook(int bookId) {
        // Check if book has any active transactions
        String checkTransactionsSQL = "SELECT COUNT(*) FROM transactions WHERE book_id = ? AND return_date IS NULL";
        List<Book> activeTransactions = executeQuery(checkTransactionsSQL, 
            new Object[]{bookId}, rs -> {
                Book book = new Book();
                book.setId(rs.getInt(1));
                return book;
            });
        
        if (!activeTransactions.isEmpty() && activeTransactions.get(0).getId() > 0) {
            throw new RuntimeException("Cannot delete book with active transactions");
        }

        return executeUpdate(DELETE_SQL, new Object[]{bookId}) > 0;
    }

    public List<Book> getAllBooks() {
        return executeQuery(SELECT_ALL_SQL, null, this::mapResultSet);
    }

    public Book getBookById(int bookId) {
        List<Book> books = executeQuery(SELECT_BY_ID_SQL, new Object[]{bookId}, this::mapResultSet);
        return books.isEmpty() ? null : books.get(0);
    }

    public List<Book> searchBooks(String searchTerm) {
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(SEARCH_SQL, 
            new Object[]{searchPattern, searchPattern, searchPattern}, 
            this::mapResultSet);
    }

    public boolean updateAvailableQuantity(int bookId, int newQuantity) {
        return executeUpdate(UPDATE_AVAILABLE_QUANTITY_SQL, 
            new Object[]{newQuantity, bookId}) > 0;
    }

    public Book getBookByISBN(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        List<Book> books = executeQuery(sql, new Object[]{isbn}, this::mapResultSet);
        return books.isEmpty() ? null : books.get(0);
    }

    private Book mapResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getInt("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setYear(rs.getInt("year"));
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailableQuantity(rs.getInt("available_quantity"));
        book.setCategory(rs.getString("category"));
        return book;
    }
} 