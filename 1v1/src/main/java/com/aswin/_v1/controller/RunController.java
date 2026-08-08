package com.aswin._v1.controller;

import com.aswin._v1.Service.CodeExecutionService;
import com.aswin._v1.Service.MatchService;
import com.aswin._v1.dto.SubmitRequest;
import com.aswin._v1.dto.SubmitResponse;
import com.aswin._v1.entity.Submission;
import com.aswin._v1.repository.SubmRepo;
import com.aswin._v1.repository.ProbRepo;
import com.aswin._v1.repository.UserRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/api/execute")
@CrossOrigin(origins = "*")
public class RunController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private SubmRepo submRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProbRepo problemRepo;

    @PostMapping("/run")
    public ResponseEntity<String> runCodeQuickly(@RequestBody Map<String, String> payload) {
        String sourceCode = payload.get("code");
        String inputData = payload.get("input"); 

        if (inputData == null || inputData.trim().isEmpty()) {
            inputData = "5\n3 2 5 1 7"; 
        }

        File submissionDir = null;
        try {
            submissionDir = codeExecutionService.createSubmissionDirectory();
            codeExecutionService.createSourceFile(submissionDir, sourceCode);
            codeExecutionService.createInputFile(submissionDir, inputData);

            String compileResult = codeExecutionService.compileCode(submissionDir);
            if (!"SUCCESS".equals(compileResult)) {
                return ResponseEntity.ok("COMPILATION ERROR:\n" + compileResult);
            }

            return ResponseEntity.ok(codeExecutionService.runCompiledCode(submissionDir));
        } finally {
            if (submissionDir != null && submissionDir.exists()) {
                deleteDirectory(submissionDir);
            }
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitResponse> submitCode(@RequestBody SubmitRequest request) {
        Submission submission = new Submission();
        submission.setSourceCode(request.getCode());
        submission.setVerdict("RUNNING");
        userRepo.findById(request.getUserId()).ifPresent(submission::setUser);
        problemRepo.findById(request.getProblemId()).ifPresent(submission::setProblem);
        
        submission = submRepo.save(submission);

        long startTime = System.currentTimeMillis();
        SubmitResponse result = codeExecutionService.evaluateCode(request);
        long endTime = System.currentTimeMillis();

        submission.setVerdict(result.getVerdict());
        submission.setDetails(result.getActualOutput());
        submission.setExecTime((endTime - startTime) / 1000.0);
        submRepo.save(submission);

        if ("ACCEPTED".equals(result.getVerdict()) && request.getRoomId() != null) {
            matchService.resolveMatch(request.getUserId(), request.getOpponentId(), request.getRoomId(), "NORMAL");
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/forfeit")
    public ResponseEntity<String> forfeitMatch(@RequestBody Map<String, Object> payload) {
        try {
            String roomId = (String) payload.get("roomId");
            Object userIdObj = payload.get("userId");
            Object opponentIdObj = payload.get("opponentId");

            
            Long loserId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : null;
            Long winnerId = opponentIdObj != null ? Long.valueOf(opponentIdObj.toString()) : null;

            if (roomId != null && loserId != null) {
                
                matchService.resolveMatch(winnerId, loserId, roomId, "FORFEIT");
            }

            return ResponseEntity.ok("Forfeited successfully");
        } catch (Exception e) {
            System.out.println("Error processing forfeit: " + e.getMessage());
            return ResponseEntity.status(500).body("Error processing forfeit");
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