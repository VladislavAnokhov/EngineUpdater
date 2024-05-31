package anokhov.EngineUpdater.controller;

import anokhov.EngineUpdater.example.NexiPreset;
import anokhov.EngineUpdater.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;

@Controller
public class CsvController {


    private final CsvService csvService;

    @Autowired
    public CsvController( CsvService csvService) {
        this.csvService = csvService;
    }

    @GetMapping("")
    public String index() {
        return "main";
    }

    @PostMapping("/uploadCsv")
    public String handleFileUpload(@RequestParam("presetType") String presetType,
                                    @RequestParam("file") MultipartFile file,
                                   @RequestParam("outputDir") String outputDir,
                                   @RequestParam("regNumberPrefix") String regNumberPrefix,
                                   @RequestParam("maxObjsPerFile") int maxObjsPerFile,
                                   RedirectAttributes redirectAttributes) {

        File outputDirectory = new File(outputDir);
        try {
            csvService.processCsv(presetType, file, outputDirectory, regNumberPrefix);
            csvService.setDEFAULT_OBJS_PER_FILE(maxObjsPerFile);
            redirectAttributes.addFlashAttribute("message", "CSV processed successfully! Check files and console");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Error processing CSV: " + e.getMessage());
        }

        return "redirect:/uploadResult";
    }
    @GetMapping("/uploadResult")
    public String uploadResult() {
        return "repeat";
    }
}
