package com.aswin._v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aswin._v1.entity.Problem; 

public interface ProbRepo extends JpaRepository<Problem, Long> {
}