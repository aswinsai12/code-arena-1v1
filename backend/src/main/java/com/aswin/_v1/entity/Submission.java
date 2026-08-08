package com.aswin._v1.entity;
import org.hibernate.annotations.Fetch;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "Submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name="problem_id",nullable = false)
     private Problem problem;

     private String language;

     @Column(columnDefinition = "TEXT")
    private String sourceCode;

     private String verdict;
     @Column(columnDefinition = "TEXT")
    private String details;

     private double ExecTime;

     private Integer memo;

    private LocalDateTime submittedAt = LocalDateTime.now();
}

