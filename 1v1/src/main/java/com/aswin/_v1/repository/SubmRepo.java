package com.aswin._v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.aswin._v1.entity.Submission; 
import java.util.*;
public interface SubmRepo extends JpaRepository<Submission, Long> {
    List<Submission> findByUserIdOrderByIdDesc(Long UserId);
}