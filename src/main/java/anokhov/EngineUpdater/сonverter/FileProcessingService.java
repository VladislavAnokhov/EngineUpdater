/*
package anokhov.EngineUpdater.сonverter;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class FileProcessingService {

    @Autowired
    private Configuration freemarkerConfig;

    public void processFile(String interfaceID, int chunkSize, MultipartFile file) throws IOException, TemplateException {
        List<Map<String, String>> data = parseCsv(file);
        generateXmlFiles(interfaceID, chunkSize, data, "standard_template.ftl");
    }

    public void processFileWithTemplate(int chunkSize, MultipartFile templateFile, MultipartFile file) throws IOException, TemplateException {
        List<Map<String, String>> data = parseCsv(file);
        String templateContent = new String(templateFile.getBytes(), StandardCharsets.UTF_8);
        Template customTemplate = new Template("customTemplate", new StringReader(templateContent), freemarkerConfig);
        generateXmlFiles(chunkSize, data, customTemplate);
    }

    private List<Map<String, String>> parseCsv(MultipartFile file) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            String[] headers = headerLine.split(",");
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                Map<String, String> record = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    record.put(headers[i], values[i]);
                }
                record.put("REG_NUMBER", generateRegNumber());
                data.add(record);
            }
        }
        return data;
    }

    private String generateRegNumber() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return LocalDateTime.now().format(formatter) + UUID.randomUUID().toString().substring(0, 5);
    }

    private void generateXmlFiles(String interfaceID, int chunkSize, List<Map<String, String>> data, String templateName) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate(templateName);
        generateXmlFiles(chunkSize, data, template);
    }

    private void generateXmlFiles(int chunkSize, List<Map<String, String>> data, Template template) throws IOException, TemplateException {
        int fileCount = (data.size() + chunkSize - 1) / chunkSize;
        for (int i = 0; i < fileCount; i++) {
            List<Map<String, String>> chunkData = data.subList(i * chunkSize, Math.min(data.size(), (i + 1) * chunkSize));
            Map<String, Object> model = new HashMap<>();
            model.put("data", chunkData);

            try (Writer fileWriter = new FileWriter("output" + i + ".xml")) {
                template.process(model, fileWriter);
            }
        }
    }
}*/
