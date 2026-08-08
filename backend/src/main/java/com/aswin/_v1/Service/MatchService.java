package com.aswin._v1.Service;

import com.aswin._v1.entity.User;
import com.aswin._v1.repository.UserRepo; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchService {

    @Autowired
    private UserRepo ur; 

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    
    private final Map<String, Long> resolvedRooms = new ConcurrentHashMap<>();

    public synchronized void resolveMatch(Long winnerId, Long loserId, String roomId, String reason) {
        
        long currentTime = System.currentTimeMillis();
        if (roomId != null && resolvedRooms.containsKey(roomId)) {
            long lastScoredTime = resolvedRooms.get(roomId);
            if (currentTime - lastScoredTime < 2000) { 
                return; 
            }
        }
        
        if (roomId != null) {
            resolvedRooms.put(roomId, currentTime);
        } 
        User winner = winnerId != null ? ur.findById(winnerId).orElse(null) : null;
        User loser = loserId != null ? ur.findById(loserId).orElse(null) : null;
        if (winner != null) {
            winner.setPoints(winner.getPoints() + 5);
            winner.setDuelsPlayed(winner.getDuelsPlayed() + 1);
            winner.setDuelsWon(winner.getDuelsWon() + 1);
            ur.save(winner);
        }
        if (loser != null) {
            loser.setPoints(Math.max(0, loser.getPoints() - 3));
            loser.setDuelsPlayed(loser.getDuelsPlayed() + 1);
            ur.save(loser); 
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MATCH_OVER");
        payload.put("winnerId", winnerId);
        payload.put("loserId", loserId);
        payload.put("reason", reason); 
        messagingTemplate.convertAndSend("/topic/duel/" + roomId, (Object)payload);
    }
}