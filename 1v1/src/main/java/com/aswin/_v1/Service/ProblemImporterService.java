package com.aswin._v1.Service;

import com.aswin._v1.entity.Problem;
import com.aswin._v1.entity.TestCase;
import com.aswin._v1.repository.ProbRepo;
import com.aswin._v1.repository.TestCaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProblemImporterService {

    @Autowired
    private TestCaseRepo testCaseRepo;

    @Autowired
    private ProbRepo probRepo;

    public String importTestCasesFromZip(MultipartFile file, Long problemId) {
        try {
            return processZipStream(file.getInputStream(), problemId);
        } catch (Exception e) {
            return "Failed to read uploaded file: " + e.getMessage();
        }
    }

    public String bulkImportFromLocalFolder(String folderPath) {
        File folder = new File(folderPath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            return "❌ Error: The folder path does not exist on your computer.";
        }

        File[] zipFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".zip"));
        
        if (zipFiles == null || zipFiles.length == 0) {
            return "⚠️ No ZIP files found in the folder.";
        }

        int totalProblemsProcessed = 0;
        int totalTestCasesSaved = 0;

        for (File zipFile : zipFiles) {
            // WRAPPED IN TRY-CATCH: If one zip fails, it prints the error and continues to the next!
            try {
                String fileName = zipFile.getName();
                Long problemId = Long.parseLong(fileName.replace(".zip", ""));

                FileInputStream fis = new FileInputStream(zipFile);
                String result = processZipStream(fis, problemId);
                
                System.out.println("✅ Processed " + fileName + ": " + result);
                totalProblemsProcessed++;
                
                if (result.contains("Successfully extracted")) {
                    String[] parts = result.split(" ");
                    if (parts.length > 2) {
                        try {
                            totalTestCasesSaved += Integer.parseInt(parts[2]);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                
            } catch (Exception e) {
                // It will log the exact problem file that failed, but KEEP GOING for the rest
                System.out.println("❌ Skipped " + zipFile.getName() + " due to error: " + e.getMessage());
            }
        }

        return "🎉 BULK IMPORT COMPLETE! Processed " + totalProblemsProcessed + " problems and saved " + totalTestCasesSaved + " hidden test cases.";
    }

    private String processZipStream(InputStream inputStream, Long problemId) {
        Optional<Problem> optionalProblem = probRepo.findById(problemId);
        if (optionalProblem.isEmpty()) {
            return "Error: Problem ID " + problemId + " does not exist in the database!";
        }
        Problem problem = optionalProblem.get();

        Map<String, String> inputs = new HashMap<>();
        Map<String, String> outputs = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String rawName = entry.getName();
                // Strip any parent directory names if present inside the zip (e.g., "tests/1.in" -> "1.in")
                String fileName = rawName.substring(rawName.lastIndexOf('/') + 1);
                fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);

                // Read file contents completely line by line
                BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }

                if (fileName.endsWith(".in")) {
                    String baseName = fileName.replace(".in", "").trim();
                    inputs.put(baseName, content.toString().trim());
                } else if (fileName.endsWith(".out")) {
                    String baseName = fileName.replace(".out", "").trim();
                    outputs.put(baseName, content.toString().trim());
                }
                
                zis.closeEntry();
            }

            int savedCount = 0;
            // Match every .in with its corresponding .out using the exact numeric key (1, 2, 10, etc.)
            for (String key : inputs.keySet()) {
                if (outputs.containsKey(key)) {
                    TestCase tc = new TestCase();
                    tc.setProblem(problem);
                    tc.setInput(inputs.get(key));
                    tc.setExpectedOutput(outputs.get(key));
                    tc.setIsHidden(true);
                    
                    testCaseRepo.save(tc);
                    savedCount++;
                }
            }

            return "Successfully extracted " + savedCount + " hidden test cases.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error reading ZIP: " + e.getMessage();
        }
    }
}