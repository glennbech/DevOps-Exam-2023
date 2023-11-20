package com.example.s3rekognition;

import java.io.Serializable;
import java.util.List;

/**
 * This is the response sent back from the Apprunner service
 *
 */
public class PPEResponse implements Serializable {

    private String bucketName;
    private List<PPEClassificationResponse> results;
    private int numberOfViolations;
    private int numberOfValid;

    public PPEResponse() {
    }

    public PPEResponse(String bucketName, List<PPEClassificationResponse> results) {
        this.bucketName = bucketName;
        this.results = results;
    }

    public String getBucketName() {
        return bucketName;
    }

    public List<PPEClassificationResponse> getResults() {
        return results;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void setResults(List<PPEClassificationResponse> results) {
        this.results = results;
    }

    public int getNumberOfViolations() {
        return numberOfViolations;
    }

    public void setNumberOfViolations(int numberOfViolations) {
        this.numberOfViolations = numberOfViolations;
    }

    public int getNumberOfValid() {
        return numberOfValid;
    }

    public void setNumberOfValid(int numberOfValid) {
        this.numberOfValid = numberOfValid;
    }
}
