
package com.aswin._v1.entity;
import org.hibernate.annotations.Fetch;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "tcs")
public class TestCase {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

@JsonIgnore
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name="problem_id",nullable = false)
      private Problem problem;

      @Column(columnDefinition = "LONGTEXT")
      private String input;

 @Column(columnDefinition = "LONGTEXT", name = "expected_output")
      private String expectedOutput;

    private Boolean isHidden;
}
