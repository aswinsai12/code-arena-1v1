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
    
    private final Map<String, Boolean> resolvedRooms = new ConcurrentHashMap<>();
    
    private final Object matchLock = new Object();

    public void resolveMatch(Long winnerId, Long loserId, String roomId, String reason) {
        
        if (roomId != null) {
            if (resolvedRooms.containsKey(roomId)) {
                return; 
            }
            resolvedRooms.put(roomId, true);
            
            if (resolvedRooms.size() > 500) {
                resolvedRooms.clear();
            }
        } 
        
        synchronized (matchLock) {
            User winner = winnerId != null ? ur.findById(winnerId).orElse(null) : null;
            User loser = loserId != null ? ur.findById(loserId).orElse(null) : null;
            
            if (winner != null) {
                winner.setPoints(winner.getPoints() + 5);
                winner.setDuelsPlayed(winner.getDuelsPlayed() + 1);
                winner.setDuelsWon(winner.getDuelsWon() + 1);
                ur.saveAndFlush(winner); 
            }
            
            if (loser != null) {
                loser.setPoints(Math.max(0, loser.getPoints() - 3));
                loser.setDuelsPlayed(loser.getDuelsPlayed() + 1);
                ur.saveAndFlush(loser); 
            }
        }
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "MATCH_OVER");
        payload.put("winnerId", winnerId);
        payload.put("loserId", loserId);
        payload.put("reason", reason); 
        messagingTemplate.convertAndSend("/topic/duel/" + roomId, (Object)payload);
    }
}