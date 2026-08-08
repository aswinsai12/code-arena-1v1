package com.aswin._v1.controller;

import com.aswin._v1.Service.CsesScraperService;
import com.aswin._v1.Service.ProblemImporterService;
import com.aswin._v1.repository.ProbRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/problems")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private CsesScraperService csesScraper;

    @Autowired
    private ProbRepo probRepo;

    
    @Autowired
    private ProblemImporterService problemImporterService;

    @GetMapping("/scrape-cses/{taskId}")
    public ResponseEntity<String> scrapeCsesProblem(@PathVariable String taskId) {
        String result = csesScraper.scrapeAndSave(taskId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<String> clearAllProblems() {
        probRepo.deleteAll();
        return ResponseEntity.ok("🗑️ All old problems have been successfully deleted from the database!");
    }

    @GetMapping("/scrape-all-cses")
    public ResponseEntity<String> scrapeAllCses() {
        new Thread(() -> {
            csesScraper.scrapeEntirePlatform();
        }).start();

        return ResponseEntity.ok("🚀 Bulk CSES scraping engine started in the background! Watch your IDE terminal logs for progress.");
    }
    @GetMapping("/bulk-import-testcases")
    public ResponseEntity<String> bulkImportTestCases(@RequestParam String folderPath) {
        
        new Thread(() -> {
            System.out.println("🚀 Starting bulk ZIP extraction from: " + folderPath);
            String result = problemImporterService.bulkImportFromLocalFolder(folderPath);
            System.out.println(result);
        }).start();

        return ResponseEntity.ok("🚀 Bulk ZIP extraction started in the background! Check your terminal for progress.");
    }

    
    
    @PostMapping("/upload-testcases")
    public ResponseEntity<String> uploadTestCases(
            @RequestParam("file") MultipartFile file,
            @RequestParam("problemId") Long problemId) {
        
        String result = problemImporterService.importTestCasesFromZip(file, problemId);
        return ResponseEntity.ok(result);
    }
}