package anokhov.EngineUpdater.service;

import anokhov.EngineUpdater.example.CsvConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class CsvService {
    private int DEFAULT_OBJS_PER_FILE = 1000;

    public void setDEFAULT_OBJS_PER_FILE(int DEFAULT_OBJS_PER_FILE ){
        if (DEFAULT_OBJS_PER_FILE==0) {
            return;
        }
        this.DEFAULT_OBJS_PER_FILE=DEFAULT_OBJS_PER_FILE;
    }

    private int getDEFAULT_OBJS_PER_FILE(){
        return DEFAULT_OBJS_PER_FILE;
    }

    public void processCsv(String presetType, MultipartFile file, File outputDirectory, String regNumberPrefix) throws IOException {
        Path tempFile = Files.createTempFile("uploaded-", file.getOriginalFilename());
        file.transferTo(tempFile.toFile());

        // Call your existing CSV processing logic here
        // Assuming generateXml is your method to process CSV and create XML
        String[] args = {presetType, tempFile.toString(), outputDirectory.toString(), regNumberPrefix};
        try {
            CsvConverter.main(args); // Adjust this method call based on your actual logic

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Error processing CSV file", e);
        } finally {
            Files.deleteIfExists(tempFile); // Clean up temporary file
        }
    }
}