package com.aswin._v1.controller;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.aswin._v1.entity.Problem;
import com.aswin._v1.entity.TestCase;
import com.aswin._v1.repository.ProbRepo;
import com.aswin._v1.repository.TestCaseRepo;
import org.springframework.web.bind.annotation.RequestBody;
@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {
    @Autowired
    private ProbRepo pr;
    @Autowired
    private TestCaseRepo tcr;
    @PostMapping("/{problemId}")
    public TestCase addTestCase(@PathVariable Long problemId,@RequestBody TestCase testCase){
        Problem p=pr.findById(problemId).orElseThrow();
        testCase.setProblem(p);
        return tcr.save(testCase);
    }
    @GetMapping("/problem/{problemId}")
public List<TestCase> getPublicTestCases(@PathVariable Long problemId) {
    return tcr.findByProblemIdAndIsHiddenFalse(problemId);
}
}
