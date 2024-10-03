package com.nilay.kompress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Controller {

    public static void compress(String filePath, String outputPath) throws IOException, SQLException {
        String status;
        long originalSize;
        long compressedSize;

        // Get original size
        originalSize = Files.size(Paths.get(filePath));

        // Compress the file
        Service.compressFile(filePath, outputPath);

        // Get compressed size
        compressedSize = Files.size(Paths.get(outputPath));
        status = "success";

        // Save action to database
        DatabaseConnection.saveFileAction(filePath, outputPath, "compress", status, originalSize, compressedSize);
    }

    public static void decompress(String filePath, String outputDir) throws IOException, SQLException {
        String status;
        long originalSize;
        long decompressedSize;

        // Get original size
        originalSize = Files.size(Paths.get(filePath));

        // Decompress the file
        Service.decompressFile(filePath, outputDir);

        // Get decompressed size
        decompressedSize = Files.size(Paths.get(outputDir, Paths.get(filePath).getFileName().toString()));
        status = "success";

        // Save action to database
        DatabaseConnection.saveFileAction(filePath, outputDir, "decompress", status, originalSize, decompressedSize);
    }
}
