package com.nilay.kompress;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class App {

    private JFrame frame;  
    private JTextField filePathField;
    private JTextField outputPathField;
    private JTable historyTable;
    private DefaultTableModel tableModel;

    // Constructor
    public App() {
        initialize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                App window = new App(); 
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void setFileChooserFont(Container container, Font font) {
        for (Component component : container.getComponents()) {
            component.setFont(font);
            if (component instanceof Container) {
                setFileChooserFont((Container) component, font);
            }
        }
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("Kompress");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        // Set a larger font for all components
        Font font = new Font("Arial", Font.PLAIN, 24);

        JLabel lblFilePath = new JLabel("File Path:");
        lblFilePath.setFont(font);
        filePathField = new JTextField(20);
        filePathField.setFont(font);
        filePathField.setPreferredSize(new Dimension(600, 50));

        JLabel lblOutputPath = new JLabel("Output Path:");
        lblOutputPath.setFont(font);
        outputPathField = new JTextField(20);
        outputPathField.setFont(font);
        outputPathField.setPreferredSize(new Dimension(600, 50));

        JButton btnCompress = new JButton("Compress");
        btnCompress.setFont(font);
        btnCompress.setPreferredSize(new Dimension(150, 40));
        btnCompress.addActionListener(e -> {
            String filePath = filePathField.getText();
            String outputPath = outputPathField.getText();
            try {
                Controller.compress(filePath, outputPath);
                JOptionPane.showMessageDialog(frame, "File compressed successfully!");
                refreshHistoryTable();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error compressing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnDecompress = new JButton("Decompress");
        btnDecompress.setFont(font);
        btnDecompress.setPreferredSize(new Dimension(150, 40));
        btnDecompress.addActionListener(e -> {
            String filePath = filePathField.getText();
            String outputPath = outputPathField.getText();
            try {
                Controller.decompress(filePath, outputPath);
                JOptionPane.showMessageDialog(frame, "File decompressed successfully!");
                refreshHistoryTable();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error decompressing file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(frame, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnBrowseFile = new JButton("Browse");
        btnBrowseFile.setFont(font);
        btnBrowseFile.setPreferredSize(new Dimension(150, 40));
        btnBrowseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setPreferredSize(new Dimension(800, 600)); 
            setFileChooserFont(fileChooser, new Font("Arial", Font.PLAIN, 24));
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton btnBrowseOutput = new JButton("Browse");
        btnBrowseOutput.setFont(font);
        btnBrowseOutput.setPreferredSize(new Dimension(150, 40));
        btnBrowseOutput.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setPreferredSize(new Dimension(800, 600));
            setFileChooserFont(fileChooser, new Font("Arial", Font.PLAIN, 24)); 
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                outputPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        tableModel = new DefaultTableModel(
            new Object[][]{},
            new String[]{"File Path", "Output Path", "Action", "Status", "Original Size", "Compressed Size"}
        );
        historyTable = new JTable(tableModel);
        historyTable.setFont(font);
        historyTable.setRowHeight(30);
        historyTable.getTableHeader().setFont(font);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblFilePath)
                        .addComponent(lblOutputPath))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(filePathField)
                        .addComponent(outputPathField))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(btnBrowseFile)
                        .addComponent(btnBrowseOutput)))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(btnCompress)
                    .addComponent(btnDecompress))
                .addComponent(scrollPane)
        );

        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFilePath)
                    .addComponent(filePathField)
                    .addComponent(btnBrowseFile))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOutputPath)
                    .addComponent(outputPathField)
                    .addComponent(btnBrowseOutput))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCompress)
                    .addComponent(btnDecompress))
                .addComponent(scrollPane)
        );

        refreshHistoryTable();
    }

    private void refreshHistoryTable() {
        try {
            ResultSet rs = DatabaseConnection.getFileActions();
            tableModel.setRowCount(0); 
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("file_path"),
                    rs.getString("output_path"),
                    rs.getString("action"),
                    rs.getString("status"),
                    rs.getLong("original_size"),
                    rs.getLong("compressed_size")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
