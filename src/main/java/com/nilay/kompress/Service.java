package com.nilay.kompress;

import java.io.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import java.nio.file.*;

public class Service {

    public static void compressFile(String filePath, String outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(fos);
             TarArchiveOutputStream aos = new TarArchiveOutputStream(gcos)) {

            Path inputFile = Paths.get(filePath);
            TarArchiveEntry entry = new TarArchiveEntry(inputFile.toFile(), inputFile.getFileName().toString());
            aos.putArchiveEntry(entry);

            Files.copy(inputFile, aos);
            aos.closeArchiveEntry();
        }
    }

    public static void decompressFile(String filePath, String outputDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             GzipCompressorInputStream gcis = new GzipCompressorInputStream(fis);
             TarArchiveInputStream tais = new TarArchiveInputStream(gcis)) {

            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tais.getNextEntry()) != null) {
                Path outputPath = Paths.get(outputDir, entry.getName());
                Files.copy(tais, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
