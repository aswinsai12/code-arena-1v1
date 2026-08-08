package com.aswin._v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aswin._v1.entity.TestCase;
import java.util.List;

public interface TestCaseRepo extends JpaRepository<TestCase, Long> {
    
    
    List<TestCase> findByProblemIdAndIsHiddenFalse(Long problemId);
    
    
    List<TestCase> findByProblemId(Long problemId);
}