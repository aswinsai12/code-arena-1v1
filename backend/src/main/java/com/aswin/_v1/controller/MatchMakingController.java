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
        System.out.println("📥 Player joined queue payload: " + payload);
        waitingPlayers.add(payload);
        
        if(waitingPlayers.size() >= 2){
            Map<String,Object> p1 = waitingPlayers.poll();
            Map<String,Object> p2 = waitingPlayers.poll();
            
            Random random = new Random();
            int randomProblemId = random.nextInt(12) + 1;
            String roomId = "room-" + UUID.randomUUID().toString().substring(0, 8);
            
            Long p1Id = parseToLong(p1.get("userId"));
            Long p2Id = parseToLong(p2.get("userId"));

            System.out.println("⚔️ MATCH FOUND! Player 1 ID: " + p1Id + " (" + p1.get("username") + 
                               ") vs Player 2 ID: " + p2Id + " (" + p2.get("username") + ")");

            // Payload for Player 1 (Opponent is Player 2)
            Map<String, Object> matchForPlayer1 = new HashMap<>();
            matchForPlayer1.put("opponent", p2.get("username"));
            matchForPlayer1.put("opponentId", p2Id);
            matchForPlayer1.put("problemId", randomProblemId);
            matchForPlayer1.put("roomId", roomId);

            // Payload for Player 2 (Opponent is Player 1)
            Map<String, Object> matchForPlayer2 = new HashMap<>();
            matchForPlayer2.put("opponent", p1.get("username"));
            matchForPlayer2.put("opponentId", p1Id);
            matchForPlayer2.put("problemId", randomProblemId);
            matchForPlayer2.put("roomId", roomId);

            System.out.println("📤 Outgoing Match Data for P1: " + matchForPlayer1);
            System.out.println("📤 Outgoing Match Data for P2: " + matchForPlayer2);

            String dest1 = "/topic/match/" + p1.get("userId");
            messagingTemplate.convertAndSend(dest1, (Object) matchForPlayer1);
            
            String dest2 = "/topic/match/" + p2.get("userId");
            messagingTemplate.convertAndSend(dest2, (Object) matchForPlayer2);
        }
    }

    private Long parseToLong(Object obj) {
        if (obj == null) return null;
        try {
            return Long.valueOf(obj.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}