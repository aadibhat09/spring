package com.open.spring.mvc.productivityPlanner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@RestController
@RequestMapping("/api/productivity-planner")
public class ProductivityPlannerController {

    @Autowired
    private ProductivityPlannerRepository repository;

    private final Dotenv dotenv = Dotenv.load();
    private final String geminiApiKey = dotenv.get("GEMINI_API_KEY");
    private final String geminiApiUrl = dotenv.get("GEMINI_API_URL");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanRequest {
        private String educationLevel;
        private int timeAvailableHours;
        private int sessionLength;
        private String peakFocusTime;
        private String[] subjects;
        private String[] learningMethods;
        private String workRestCycle;
        private String distractionTolerance;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlan(@RequestBody PlanRequest request) {
        try {
            // Build the prompt
            String prompt = String.format("""
                You are an expert productivity coach and study planner.
                Create a concise study plan based on the following user profile:
                - Education Level: %s
                - Time Available: %d hours
                - Preferred Session Length: %d minutes
                - Peak Focus Time: %s
                - Subjects: %s
                - Learning Methods: %s
                - Work/Rest Cycle: %s
                - Distraction Tolerance: %s
                
                Provide a structured, actionable study plan.
                Additionally, provide a list of suggested websites to whitelist for these subjects and learning methods.
                
                Return the result EXACTLY as a JSON object with two properties:
                1. "response": A string containing the study plan (use plain text or basic markdown).
                2. "suggestedWhitelist": An array of strings containing domain names (e.g., "github.com", "instructure.com", "wikipedia.org"). Do not include "https://" or "www.".
                
                Do not include any other text outside the JSON object. Do not wrap it in markdown code blocks like ```json.
            """, 
            request.getEducationLevel(), 
            request.getTimeAvailableHours(), 
            request.getSessionLength(), 
            request.getPeakFocusTime(), 
            Arrays.toString(request.getSubjects()), 
            Arrays.toString(request.getLearningMethods()), 
            request.getWorkRestCycle(), 
            request.getDistractionTolerance());

            // Proper JSON payload with escaped characters
            String jsonPayload = String.format("""
                {
                    "contents": [{
                        "parts": [{
                            "text": "%s"    
                        }]
                    }],
                    "generationConfig": {
                        "temperature": 0.7,
                        "maxOutputTokens": 1024
                    }
                }
            """, prompt.replace("\"", "\\\"").replace("\n", "\\n"));

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpRequest = new HttpEntity<>(jsonPayload, headers);
            String fullUrl = geminiApiUrl + "?key=" + geminiApiKey;

            // Send POST request to Gemini API
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(fullUrl, HttpMethod.POST, httpRequest, String.class);

            String resultBody = response.getBody();

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resultBody);
            
            String generatedText = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text").asText();
                    
            // The generatedText should be a JSON string because we asked for JSON
            JsonNode generatedJson = mapper.readTree(generatedText);
            
            String planResponse = generatedJson.path("response").asText();
            JsonNode whitelistNode = generatedJson.path("suggestedWhitelist");
            
            String whitelistStr = whitelistNode.toString(); // Store as JSON string in DB

            // Persist to DB
            ProductivityPlanner record = new ProductivityPlanner(
                request.getEducationLevel(),
                request.getTimeAvailableHours(),
                request.getSessionLength(),
                request.getPeakFocusTime(),
                Arrays.toString(request.getSubjects()),
                Arrays.toString(request.getLearningMethods()),
                request.getWorkRestCycle(),
                request.getDistractionTolerance()
            );
            record.setResponse(planResponse);
            record.setSuggestedWhitelist(whitelistStr);
            ProductivityPlanner saved = repository.save(record);

            // Return payload
            return ResponseEntity.ok(
                Map.of(
                    "id", saved.getId(),
                    "response", planResponse,
                    "suggestedWhitelist", mapper.convertValue(whitelistNode, List.class)
                )
            );

        } catch (HttpClientErrorException.TooManyRequests e) {
            return ResponseEntity.status(429).body(Map.of("error", "Gemini quota exceeded. Please try again later."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        List<ProductivityPlanner> results = repository.findAll();
        return ResponseEntity.ok(Map.of(
            "count", results.size(),
            "results", results
        ));
    }
}
