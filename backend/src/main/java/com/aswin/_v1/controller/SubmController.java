package com.aswin._v1.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import com.aswin._v1.repository.UserRepo;
import com.aswin._v1.repository.SubmRepo;
import com.aswin._v1.repository.ProbRepo;
import com.aswin._v1.entity.Problem;
import com.aswin._v1.entity.Submission;
import com.aswin._v1.entity.User;
import com.aswin._v1.Service.MatchService; // Make sure to import MatchService
import java.util.*;

@RestController
@RequestMapping("/api/submissions")
public class SubmController {

    @Autowired
    private UserRepo ur;

    @Autowired
    private SubmRepo sr;

    @Autowired
    private ProbRepo pr;

    @Autowired 
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MatchService matchService; // Inject MatchService

    @PostMapping("/submit/{userId}/{problemId}")
    public Submission submitCode(
            @PathVariable Long userId, 
            @PathVariable Long problemId, 
            @RequestParam(required = false) Long opponentId, // Accepts opponentId if in a duel
            @RequestParam(required = false) String roomId,   // Accepts roomId if in a duel
            @RequestBody Submission submission) {

        User u = ur.findById(userId).orElseThrow();
        Problem p = pr.findById(problemId).orElseThrow();

        submission.setUser(u);
        submission.setProblem(p);
        submission.setVerdict("PENDING");

        Submission savedSubmission = sr.save(submission);

        // Send submission to RabbitMQ queue for execution
        rabbitTemplate.convertAndSend("submission_queue", String.valueOf(savedSubmission.getId()));

        return savedSubmission;
    }

    @GetMapping("/{id}")
    public Submission getSubmissionStatus(@PathVariable Long id) {
        return sr.findById(id).orElseThrow();
    }

    @GetMapping("/history/{userId}")
    public List<Submission> getSubmissionsbyId(@PathVariable Long userId) {
        return sr.findByUserIdOrderByIdDesc(userId);
    }
}