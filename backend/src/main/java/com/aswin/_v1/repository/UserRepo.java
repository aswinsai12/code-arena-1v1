package com.aswin._v1.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import com.aswin._v1.entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByRatingDesc(Long userId);
   
}
