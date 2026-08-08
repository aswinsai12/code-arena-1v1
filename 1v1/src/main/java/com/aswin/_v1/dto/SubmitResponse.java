package com.aswin._v1.dto;

public class SubmitResponse {
    private String verdict; 
    private String failedInput;
    private String expectedOutput;
    private String actualOutput;

    
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
    public String getFailedInput() { return failedInput; }
    public void setFailedInput(String failedInput) { this.failedInput = failedInput; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
}