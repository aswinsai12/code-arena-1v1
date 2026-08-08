package com.aswin._v1.controller;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchMakingController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    private Queue<Map<String, Object>> waitingPlayers = new ConcurrentLinkedQueue<>();
    
    @MessageMapping("/joinQueue")
    public void JoinQueue(Map<String,Object> payload){
        waitingPlayers.add(payload);
        
        if(waitingPlayers.size() >= 2){
            Map<String,Object> p1 = waitingPlayers.poll();
            Map<String,Object> p2 = waitingPlayers.poll();
            Random random = new Random();
            int randomProblemId = random.nextInt(12)+1;
            
            Map<String, Object> matchForPlayer1 = Map.of(
                "opponent", p2.get("username"),
                "problemId", randomProblemId
            );

            Map<String, Object> matchForPlayer2 = Map.of(
                "opponent", p1.get("username"),
                "problemId", randomProblemId
            );
            String dest1 = "/topic/match/" + p1.get("userId");
            messagingTemplate.convertAndSend(dest1, (Object) matchForPlayer1);
            
            String dest2 = "/topic/match/" + p2.get("userId");
            messagingTemplate.convertAndSend(dest2, (Object) matchForPlayer2);
        }
    }
}