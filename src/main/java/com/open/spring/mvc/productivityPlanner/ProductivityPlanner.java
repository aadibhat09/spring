package com.open.spring.mvc.productivityPlanner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ProductivityPlanner {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String educationLevel;
    private int timeAvailableHours;
    private int sessionLength;
    private String peakFocusTime;
    private String subjects;
    private String learningMethods;
    private String workRestCycle;
    private String distractionTolerance;

    @Column(columnDefinition = "TEXT")
    private String response;

    @Column(columnDefinition = "TEXT")
    private String suggestedWhitelist;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    public ProductivityPlanner(String educationLevel, int timeAvailableHours, int sessionLength, String peakFocusTime, String subjects, String learningMethods, String workRestCycle, String distractionTolerance) {
        this.educationLevel = educationLevel;
        this.timeAvailableHours = timeAvailableHours;
        this.sessionLength = sessionLength;
        this.peakFocusTime = peakFocusTime;
        this.subjects = subjects;
        this.learningMethods = learningMethods;
        this.workRestCycle = workRestCycle;
        this.distractionTolerance = distractionTolerance;
        this.createdAt = System.currentTimeMillis();
    }
}
