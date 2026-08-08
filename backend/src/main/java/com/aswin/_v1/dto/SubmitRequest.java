package com.aswin._v1.dto;

public class SubmitRequest {
    private Long problemId;
    private Long userId;
    private String code;
    
    
    private String roomId;
    private Long opponentId;

    
    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public Long getOpponentId() { return opponentId; }
    public void setOpponentId(Long opponentId) { this.opponentId = opponentId; }
}