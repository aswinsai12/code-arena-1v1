package com.aswin._v1.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aswin._v1.dto.SubmitRequest;
import com.aswin._v1.dto.SubmitResponse;
import com.aswin._v1.entity.TestCase;
import com.aswin._v1.repository.TestCaseRepo;

@Service
public class CodeExecutionService {

    @Autowired
    private TestCaseRepo testCaseRepo;

    
    public File createSubmissionDirectory() {
        String uniqueFolder = UUID.randomUUID().toString();
        File dir = new File("temp_submissions/" + uniqueFolder);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    
    public void createSourceFile(File dir, String sourceCode) {
        File file = new File(dir, "Main.java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(sourceCode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void createInputFile(File dir, String inputData) {
        File file = new File(dir, "input.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(inputData != null ? inputData : "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public String compileCode(File dir) {
        try {
            Path outputPath = Paths.get(dir.getAbsolutePath(), "compile_error.txt");

            String[] command = {
                "docker", "run", "--rm",
                "-v", dir.getAbsolutePath() + ":/app",
                "-w", "/app",
                "eclipse-temurin:17",
                "javac", "Main.java"
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectError(outputPath.toFile());

            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "COMPILATION_TIMEOUT";
            }

            if (Files.exists(outputPath) && Files.size(outputPath) > 0) {
                return Files.readString(outputPath);
            }

            return "SUCCESS";

        } catch (Exception e) {
            return "SYSTEM_ERROR: " + e.getMessage();
        }
    }

    
    public String runCompiledCode(File dir) {
        try {
            Path outputPath = Paths.get(dir.getAbsolutePath(), "output.txt");
            Path errorPath = Paths.get(dir.getAbsolutePath(), "error.txt");

            String[] command = {
                "docker", "run", "--rm",
                "-v", dir.getAbsolutePath() + ":/app",
                "-w", "/app",
                "eclipse-temurin:17",
                "sh", "-c", "java Main < input.txt"
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectOutput(outputPath.toFile());
            pb.redirectError(errorPath.toFile());

            Process process = pb.start();

            
            boolean finished = process.waitFor(2, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return "TIME_LIMIT_EXCEEDED";
            }

            if (Files.exists(errorPath) && Files.size(errorPath) > 0) {
                String error = Files.readString(errorPath);
                if (!error.trim().isEmpty()) {
                    return "ERROR:\n" + error;
                }
            }

            return Files.exists(outputPath) ? Files.readString(outputPath) : "";

        } catch (Exception e) {
            return "SYSTEM ERROR: " + e.getMessage();
        }
    }

    
    public SubmitResponse evaluateCode(SubmitRequest request) {
        SubmitResponse response = new SubmitResponse();
        List<TestCase> testCases = testCaseRepo.findByProblemId(request.getProblemId());

        
        File submissionDir = createSubmissionDirectory();

        try {
            
            createSourceFile(submissionDir, request.getCode());

            
            String compileResult = compileCode(submissionDir);
            if (!"SUCCESS".equals(compileResult)) {
                response.setVerdict("ERROR");
                response.setActualOutput(compileResult);
                return response;
            }

            
            for (TestCase tc : testCases) {
                createInputFile(submissionDir, tc.getInput());

                String actualOutput = runCompiledCode(submissionDir);

                if ("TIME_LIMIT_EXCEEDED".equals(actualOutput)) {
                    response.setVerdict("TIME_LIMIT_EXCEEDED");
                    response.setActualOutput("Time Limit Exceeded (2.0s)");
                    return response;
                }

                if (actualOutput.startsWith("ERROR:") || actualOutput.startsWith("SYSTEM ERROR:")) {
                    response.setVerdict("ERROR");
                    response.setActualOutput(actualOutput);
                    return response;
                }

                if (!actualOutput.trim().equals(tc.getExpectedOutput().trim())) {
                    response.setVerdict("WRONG_ANSWER");
                    response.setFailedInput(tc.getInput());
                    response.setExpectedOutput(tc.getExpectedOutput());
                    response.setActualOutput(actualOutput.trim());
                    return response;
                }
            }

            response.setVerdict("ACCEPTED");
            return response;

        } finally {
            
            deleteDirectory(submissionDir);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
        dir.delete();
    }
}