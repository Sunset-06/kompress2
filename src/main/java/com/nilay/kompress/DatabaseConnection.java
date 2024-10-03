package com.nilay.kompress;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/kompressdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "admin";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS files (" +
                     "id SERIAL PRIMARY KEY, " +
                     "file_path VARCHAR(255) NOT NULL, " +
                     "output_path VARCHAR(255) NOT NULL, " +
                     "action VARCHAR(50) NOT NULL, " +
                     "status VARCHAR(50) NOT NULL, " +
                     "original_size BIGINT NOT NULL, " +
                     "compressed_size BIGINT NOT NULL" +
                     ")";
        try (Connection conn = getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public static void saveFileAction(String filePath, String outputPath, String action, String status, long originalSize, long compressedSize) throws SQLException {
        createTableIfNotExists(); 
        
        Connection conn = getConnection();

        String insertSql = "INSERT INTO files (file_path, output_path, action, status, original_size, compressed_size) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setString(1, filePath);
            insertStmt.setString(2, outputPath);
            insertStmt.setString(3, action);
            insertStmt.setString(4, status);
            insertStmt.setLong(5, originalSize);
            insertStmt.setLong(6, compressedSize);
            insertStmt.executeUpdate();
        }

        String deleteSql = "DELETE FROM files WHERE id NOT IN (SELECT id FROM files ORDER BY id DESC LIMIT 10)";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.executeUpdate();
        }
    }

    public static ResultSet getFileActions() throws SQLException {
        createTableIfNotExists(); 

        Connection conn = getConnection();
        String sql = "SELECT * FROM files ORDER BY id DESC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public static void main(String[] args) {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
