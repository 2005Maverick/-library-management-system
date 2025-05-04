package com.library.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:library.db";
    private static DatabaseManager instance;
    private Connection connection;
    private boolean initialized = false;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Reopen the connection if closed
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }

    private synchronized void initializeDatabase() {
        if (initialized) {
            return;
        }

        try (Statement statement = connection.createStatement()) {
            // Enable foreign keys
            statement.execute("PRAGMA foreign_keys = ON");

            // Create Users table
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT UNIQUE NOT NULL," +
                    "password TEXT NOT NULL," +
                    "name TEXT NOT NULL," +
                    "email TEXT UNIQUE NOT NULL," +
                    "user_type TEXT NOT NULL CHECK(user_type IN ('ADMIN', 'LIBRARIAN', 'STUDENT'))" +
                    ")");

            // Create Books table
            statement.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "title TEXT NOT NULL," +
                    "author TEXT NOT NULL," +
                    "isbn TEXT UNIQUE NOT NULL," +
                    "publisher TEXT," +
                    "year INTEGER CHECK(year > 0 AND year <= 9999)," +
                    "quantity INTEGER NOT NULL CHECK(quantity >= 0)," +
                    "available_quantity INTEGER NOT NULL CHECK(available_quantity >= 0 AND available_quantity <= quantity)," +
                    "category TEXT" +
                    ")");

            // Drop existing transactions table if it exists
            statement.execute("DROP TABLE IF EXISTS transactions");

            // Create Transactions table with correct schema
            statement.execute("CREATE TABLE transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "book_id INTEGER NOT NULL," +
                    "user_id INTEGER NOT NULL," +
                    "issue_date DATETIME," +
                    "due_date DATETIME," +
                    "return_date DATETIME," +
                    "fine REAL DEFAULT 0 CHECK(fine >= 0)," +
                    "status TEXT NOT NULL CHECK(status IN ('REQUESTED', 'ISSUED', 'RETURNED', 'OVERDUE', 'REJECTED'))," +
                    "FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE RESTRICT," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT" +
                    ")");

            // Create indexes
            statement.execute("CREATE INDEX IF NOT EXISTS idx_books_title ON books(title)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_books_author ON books(author)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_book ON transactions(book_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status)");

            // Insert default users and books
            insertDefaultUsers();
            insertDefaultBooks();

            initialized = true;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    private void insertDefaultUsers() {
        try (Statement statement = connection.createStatement()) {
            // Insert default admin user
            statement.execute("INSERT OR IGNORE INTO users (username, password, name, email, user_type) VALUES " +
                    "('admin', 'admin123', 'System Admin', 'admin@library.com', 'ADMIN')");
            
            // Insert default librarian user
            statement.execute("INSERT OR IGNORE INTO users (username, password, name, email, user_type) VALUES " +
                    "('librarian', 'librarian123', 'Library Staff', 'librarian@library.com', 'LIBRARIAN')");
            
            // Insert multiple default student users
            String[][] students = {
                {"pranav", "pranav123", "Pranav Singh Puri", "pranav@student.com", "STUDENT"},
                {"rudra", "rudra123", "Rudra Gupta", "rudra@student.com", "STUDENT"},
                {"anirudh", "anirudh123", "Aniruddh Vijay Vargia", "anirudh@student.com", "STUDENT"},
                {"saksham", "saksham123", "Saksham Pathak", "saksham@student.com", "STUDENT"}
            };

            for (String[] student : students) {
                statement.execute(String.format(
                    "INSERT OR IGNORE INTO users (username, password, name, email, user_type) VALUES " +
                    "('%s', '%s', '%s', '%s', '%s')",
                    student[0], student[1], student[2], student[3], student[4]
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert default users: " + e.getMessage(), e);
        }
    }

    private void insertDefaultBooks() {
        try (Statement statement = connection.createStatement()) {
            // Insert 50 real books
            String[][] books = {
                {"To Kill a Mockingbird", "Harper Lee", "9780446310789", "Grand Central Publishing", "1960", "Fiction"},
                {"1984", "George Orwell", "9780451524935", "Signet Classic", "1949", "Science Fiction"},
                {"Pride and Prejudice", "Jane Austen", "9780141439518", "Penguin Classics", "1813", "Romance"},
                {"The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", "Scribner", "1925", "Fiction"},
                {"The Catcher in the Rye", "J.D. Salinger", "9780316769488", "Little, Brown and Company", "1951", "Fiction"},
                {"The Hobbit", "J.R.R. Tolkien", "9780547928227", "Houghton Mifflin Harcourt", "1937", "Fantasy"},
                {"The Lord of the Rings", "J.R.R. Tolkien", "9780618640157", "Houghton Mifflin Harcourt", "1954", "Fantasy"},
                {"Animal Farm", "George Orwell", "9780451526342", "Signet Classic", "1945", "Political Satire"},
                {"Brave New World", "Aldous Huxley", "9780060850524", "Harper Perennial", "1932", "Science Fiction"},
                {"The Chronicles of Narnia", "C.S. Lewis", "9780064404990", "HarperCollins", "1950", "Fantasy"},
                {"The Da Vinci Code", "Dan Brown", "9780307474278", "Anchor", "2003", "Mystery"},
                {"The Alchemist", "Paulo Coelho", "9780062315007", "HarperOne", "1988", "Fiction"},
                {"The Kite Runner", "Khaled Hosseini", "9781594631931", "Riverhead Books", "2003", "Fiction"},
                {"The Book Thief", "Markus Zusak", "9780375842207", "Knopf Books for Young Readers", "2005", "Historical Fiction"},
                {"The Hunger Games", "Suzanne Collins", "9780439023481", "Scholastic Press", "2008", "Science Fiction"},
                {"The Fault in Our Stars", "John Green", "9780525478812", "Dutton Books", "2012", "Young Adult"},
                {"Gone Girl", "Gillian Flynn", "9780307588364", "Crown Publishing Group", "2012", "Thriller"},
                {"The Girl with the Dragon Tattoo", "Stieg Larsson", "9780307269751", "Vintage Crime/Black Lizard", "2005", "Mystery"},
                {"The Help", "Kathryn Stockett", "9780425232200", "Berkley", "2009", "Historical Fiction"},
                {"The Martian", "Andy Weir", "9780553418026", "Broadway Books", "2011", "Science Fiction"},
                {"The Night Circus", "Erin Morgenstern", "9780307744432", "Anchor", "2011", "Fantasy"},
                {"The Goldfinch", "Donna Tartt", "9780316055437", "Little, Brown and Company", "2013", "Fiction"},
                {"The Silent Patient", "Alex Michaelides", "9781250301697", "Celadon Books", "2019", "Psychological Thriller"},
                {"Educated", "Tara Westover", "9780399590504", "Random House", "2018", "Memoir"},
                {"Where the Crawdads Sing", "Delia Owens", "9780735219090", "G.P. Putnam's Sons", "2018", "Fiction"},
                {"The Midnight Library", "Matt Haig", "9780525559474", "Viking", "2020", "Fiction"},
                {"Project Hail Mary", "Andy Weir", "9780593135204", "Ballantine Books", "2021", "Science Fiction"},
                {"The Seven Husbands of Evelyn Hugo", "Taylor Jenkins Reid", "9781501139239", "Washington Square Press", "2017", "Historical Fiction"},
                {"Normal People", "Sally Rooney", "9781984822185", "Hogarth", "2018", "Fiction"},
                {"The Vanishing Half", "Brit Bennett", "9780525536291", "Riverhead Books", "2020", "Fiction"},
                {"The Invisible Life of Addie LaRue", "V.E. Schwab", "9780765387561", "Tor Books", "2020", "Fantasy"},
                {"Klara and the Sun", "Kazuo Ishiguro", "9780593318171", "Knopf", "2021", "Science Fiction"},
                {"The House in the Cerulean Sea", "TJ Klune", "9781250217288", "Tor Books", "2020", "Fantasy"},
                {"Pachinko", "Min Jin Lee", "9781455563937", "Grand Central Publishing", "2017", "Historical Fiction"},
                {"The Song of Achilles", "Madeline Miller", "9780062060624", "Ecco", "2011", "Historical Fiction"},
                {"Circe", "Madeline Miller", "9780316556347", "Little, Brown and Company", "2018", "Fantasy"},
                {"The Power", "Naomi Alderman", "9780316547604", "Little, Brown and Company", "2016", "Science Fiction"},
                {"The Testaments", "Margaret Atwood", "9780385543781", "Nan A. Talese", "2019", "Science Fiction"},
                {"The Water Dancer", "Ta-Nehisi Coates", "9780399590597", "One World", "2019", "Historical Fiction"},
                {"The Dutch House", "Ann Patchett", "9780062963673", "Harper", "2019", "Fiction"},
                {"Such a Fun Age", "Kiley Reid", "9780523508194", "G.P. Putnam's Sons", "2019", "Fiction"},
                {"The Guest List", "Lucy Foley", "9780062868930", "William Morrow", "2020", "Thriller"},
                {"The Thursday Murder Club", "Richard Osman", "9781984880987", "Pamela Dorman Books", "2020", "Mystery"},
                {"The Midnight Library", "Matt Haig", "9780525559474", "Viking", "2020", "Fiction"},
                {"The Push", "Ashley Audrain", "9781984881663", "Pamela Dorman Books", "2021", "Psychological Thriller"},
                {"The Four Winds", "Kristin Hannah", "9781250178602", "St. Martin's Press", "2021", "Historical Fiction"},
                {"Malibu Rising", "Taylor Jenkins Reid", "9781524791259", "Ballantine Books", "2021", "Fiction"},
                {"The Last Thing He Told Me", "Laura Dave", "9781501171345", "Simon & Schuster", "2021", "Thriller"},
                {"Cloud Cuckoo Land", "Anthony Doerr", "9781982168441", "Scribner", "2021", "Fiction"},
                {"Beautiful World, Where Are You", "Sally Rooney", "9780374602604", "Farrar, Straus and Giroux", "2021", "Fiction"}
            };

            for (String[] book : books) {
                statement.execute(String.format(
                    "INSERT OR IGNORE INTO books (title, author, isbn, publisher, year, quantity, available_quantity, category) " +
                    "VALUES ('%s', '%s', '%s', '%s', %s, 5, 5, '%s')",
                    book[0].replace("'", "''"),
                    book[1].replace("'", "''"),
                    book[2],
                    book[3].replace("'", "''"),
                    book[4],
                    book[5]
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert default books: " + e.getMessage(), e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                initialized = false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection: " + e.getMessage(), e);
        }
    }
} 