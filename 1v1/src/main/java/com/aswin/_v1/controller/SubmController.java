package com.aswin._v1.controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aswin._v1.repository.UserRepo;
import com.aswin._v1.repository.SubmRepo;
import com.aswin._v1.repository.ProbRepo;
import com.aswin._v1.entity.Problem;
import com.aswin._v1.entity.Submission;
import com.aswin._v1.entity.User;
import java.util.*;
@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmController{
    @Autowired
    private UserRepo ur;
    @Autowired
    private SubmRepo sr;
    @Autowired
    private ProbRepo pr;
    @Autowired 
    private RabbitTemplate rabbitTemplate;

@PostMapping("/submit/{userId}/{problemId}")
public Submission submitCode(@PathVariable Long userId, @PathVariable Long problemId, @RequestBody Submission submission) {
    
    
    User u = ur.findById(userId).orElseThrow();
    Problem p = pr.findById(problemId).orElseThrow();
    
    
    submission.setUser(u);
    submission.setProblem(p);
    submission.setVerdict("PENDING");
    
    
    Submission savedSubmission = sr.save(submission);
    
    
    rabbitTemplate.convertAndSend("submission_queue", String.valueOf(savedSubmission.getId()));
    
    return savedSubmission;
}
@GetMapping("/{id}")
    public Submission getSubmissionStatus(@PathVariable Long id) {
        return sr.findById(id).orElseThrow();
    }
    @GetMapping("/history/{userId}")
    public List<Submission> getSubmissionsbyId(@PathVariable Long userId){
         return sr.findByUserIdOrderByIdDesc(userId);

    }
}