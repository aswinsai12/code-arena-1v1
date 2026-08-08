package com.aswin._v1.entity;
import org.hibernate.annotations.Fetch;


import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
@Table(name = "users")
public class User{

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private long id;

       @Column(unique = true,nullable = false)
       private String username;

        private String email;

       private Integer rating=100;
      
    @Column(name = "duels_played", columnDefinition = "INT DEFAULT 0")
    private Integer duelsPlayed = 0;

    @Column(name = "duels_won", columnDefinition = "INT DEFAULT 0")
    private Integer duelsWon = 0;
    public Integer getDuelsPlayed() {
        return duelsPlayed;
    }

    public void setDuelsPlayed(Integer duelsPlayed) {
        this.duelsPlayed = duelsPlayed;
    }

    public Integer getDuelsWon() {
        return duelsWon;
    }

    public void setDuelsWon(Integer duelsWon) {
        this.duelsWon = duelsWon;
    }
     public Integer getPoints() {
        return rating;
    }
public void setPoints(Integer rating) {
        this.rating =rating;
    }

}
