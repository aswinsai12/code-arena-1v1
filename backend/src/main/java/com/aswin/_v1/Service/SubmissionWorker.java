package com.aswin._v1.Service;

import java.io.File;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aswin._v1.entity.Submission;
import com.aswin._v1.entity.TestCase;
import com.aswin._v1.repository.SubmRepo;

@Service
public class SubmissionWorker {

    @Autowired
    private SubmRepo sr;

    @Autowired
    private CodeExecutionService codeExecutionService;

    // 1. Inject MatchService so the worker can score the match
    @Autowired
    private MatchService matchService;

    @Transactional
    @RabbitListener(queues = "submission_queue")
    public void processSubmission(String SubmId) {
        File submissionDir = null;

        try {
            System.out.println("🔥 WORKER WOKE UP! Processing Submission ID: " + SubmId);
            Long id = Long.parseLong(SubmId);

            Submission s = sr.findById(id).orElse(null);
            if (s == null) {
                System.out.println("❌ WORKER STOPPED: Submission ID " + SubmId + " not found in database.");
                return;
            }

            long startTime = System.currentTimeMillis();

            s.setVerdict("RUNNING");
            sr.save(s);

            List<TestCase> tcs = s.getProblem().getTestCases();
            if (tcs == null || tcs.isEmpty()) {
                s.setVerdict("SYSTEM ERROR");
                s.setDetails("The admins have not uploaded test cases for this problem yet.");
                sr.save(s);
                System.out.println("❌ WORKER STOPPED: No test cases found for problem ID.");
                return;
            }

            submissionDir = codeExecutionService.createSubmissionDirectory();
            codeExecutionService.createSourceFile(submissionDir, s.getSourceCode());

            String compileResult = codeExecutionService.compileCode(submissionDir);
            if (!"SUCCESS".equals(compileResult)) {
                s.setVerdict("COMPILATION ERROR");
                s.setDetails(compileResult);
                sr.save(s);
                System.out.println("❌ COMPILATION ERROR for ID: " + SubmId);
                return;
            }

            boolean passedAll = true;

            for (int i = 0; i < tcs.size(); i++) {
                TestCase tc = tcs.get(i);
                codeExecutionService.createInputFile(submissionDir, tc.getInput());

                String executionResult = codeExecutionService.runCompiledCode(submissionDir);

                if ("TIME_LIMIT_EXCEEDED".equals(executionResult)) {
                    s.setVerdict("TIME_LIMIT_EXCEEDED");
                    s.setDetails("Time Limit Exceeded on Testcase " + (i + 1));
                    sr.save(s);
                    passedAll = false;
                    break;
                }

                if (executionResult.startsWith("ERROR:") || executionResult.startsWith("SYSTEM ERROR:")) {
                    s.setVerdict("RUNTIME ERROR");
                    s.setDetails(executionResult);
                    sr.save(s);
                    passedAll = false;
                    break;
                }

                String expected = tc.getExpectedOutput() != null ? tc.getExpectedOutput().trim() : "";
                String actual = executionResult.trim();

                if (!actual.equals(expected)) {
                    s.setVerdict("WRONG ANSWER");
                    s.setDetails("Failed on Test Case " + (i + 1) + 
                                 "\n\nInput:\n" + tc.getInput() + 
                                 "\n\nExpected Output:\n" + expected + 
                                 "\n\nYour Output:\n" + actual);
                    sr.save(s);
                    passedAll = false;
                    break;
                }
            }

            if (passedAll) {
                long endTime = System.currentTimeMillis();
                double execTimeInSeconds = (endTime - startTime) / 1000.0;

                s.setExecTime(execTimeInSeconds);
                s.setVerdict("ACCEPTED");
                s.setDetails("All test cases passed successfully!");
                sr.save(s);

                System.out.println("✅ WORKER FINISHED! ID: " + SubmId + " is ACCEPTED in " + execTimeInSeconds + "s.");

                // 2. TRIGGER MATCH RESOLUTION FOR BOTH USERS
                Long winnerId = s.getUser().getId();
                Long loserId = s.getOpponentId(); // Reads opponent ID from submission
                String roomId = s.getRoomId();

                if (roomId != null) {
                    System.out.println("🏆 RESOLVING MATCH IN WORKER: Winner=" + winnerId + " | Loser=" + loserId + " | Room=" + roomId);
                    matchService.resolveMatch(winnerId, loserId, roomId, "ACCEPTED");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ ERROR: Worker crashed processing ID: " + SubmId);
            e.printStackTrace();
        } finally {
            if (submissionDir != null && submissionDir.exists()) {
                deleteDirectory(submissionDir);
            }
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