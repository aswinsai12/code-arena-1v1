package com.aswin._v1.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aswin._v1.entity.Problem;
import com.aswin._v1.repository.ProbRepo;
@RestController
@RequestMapping("/api/problems")
@CrossOrigin(origins = "*")
public class ProbController {
    @Autowired
    private ProbRepo pr;
   @GetMapping("/{id}")
    public Problem getProblemById(@PathVariable Long id) {
        return pr.findById(id).orElseThrow(() -> new RuntimeException("Problem not found"));
    }
    @GetMapping("/all")
    public java.util.List<Problem> getAllProblems() {
        return pr.findAll();
    }
    @Autowired
    private com.aswin._v1.Service.ScraperService scraperService;
    @GetMapping("/scrape/{contestId}/{index}")
    public String testScraper(@PathVariable String contestId, @PathVariable String index) {
        return scraperService.scrapeProblemDescription(contestId, index);
    }
}
