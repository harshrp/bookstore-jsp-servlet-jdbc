package org.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private String dbUrl;

    private String dbDriver;
    private String dbUsername;
    private String dbPassword;

    @Override
    public void init() throws ServletException {
        super.init();
        loadDatabaseProperties();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Book> books = new ArrayList<>();
        try {
            // Load the MySQL JDBC driver
            Class.forName(dbDriver);
            // Connect to the database
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT * FROM books")) {
                while (resultSet.next()) {
                    Book book = new Book();
                    book.setId(resultSet.getLong("id"));
                    book.setTitle(resultSet.getString("title"));
                    book.setAuthor(resultSet.getString("author"));
                    books.add(book);
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("books", books);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action != null) {
            switch (action) {
                case "add":
                    addBook(request, response);
                    break;
                case "edit":
                    editBook(request, response);
                    break;
                case "delete":
                    deleteBook(request, response);
                    break;
            }
        }
    }

    private void addBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        try {
            Class.forName(dbDriver);
            // Connect to the database
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement statement = connection.prepareStatement("INSERT INTO books (title, author) VALUES (?, ?)")) {
                statement.setString(1, title);
                statement.setString(2, author);
                statement.executeUpdate();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/books");
    }

    private void editBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        try {
            Class.forName(dbDriver);
            // Connect to the database
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement statement = connection.prepareStatement("UPDATE books SET title=?, author=? WHERE id=?")) {
                statement.setString(1, title);
                statement.setString(2, author);
                statement.setLong(3, id);
                statement.executeUpdate();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/books");
    }

    private void deleteBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        try {
            Class.forName(dbDriver);
            // Connect to the database
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE id=?")) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        response.sendRedirect(request.getContextPath() + "/books");
    }

    private void loadDatabaseProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            dbUrl = prop.getProperty("db.url");
            dbUsername = prop.getProperty("db.username");
            dbPassword = prop.getProperty("db.password");
            dbDriver = prop.getProperty("db.driver");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
