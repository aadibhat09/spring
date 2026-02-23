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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/productivity-planner")
public class ProductivityPlannerController {

    @Autowired
    private ProductivityPlannerRepository repository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:}")
    private String geminiApiUrl;

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
            if (request == null) {
                return ResponseEntity.ok(buildFallbackPlan(null));
            }
            if (geminiApiKey == null || geminiApiKey.isBlank()) {
                return ResponseEntity.ok(buildFallbackPlan(request));
            }
            if (geminiApiUrl == null || geminiApiUrl.isBlank()) {
                return ResponseEntity.ok(buildFallbackPlan(request));
            }
            // Build the prompt
            String subjectsText = request.getSubjects() == null ? "[]" : Arrays.toString(request.getSubjects());
            String methodsText = request.getLearningMethods() == null ? "[]" : Arrays.toString(request.getLearningMethods());

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
            subjectsText, 
            methodsText, 
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
            if (resultBody == null || resultBody.isBlank()) {
                return ResponseEntity.status(502).body(Map.of("error", "Empty response from Gemini"));
            }

            // Parse response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resultBody);

            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return ResponseEntity.status(502).body(Map.of("error", "Unexpected Gemini response", "details", resultBody));
            }

            String generatedText = candidates
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text").asText();

            if (generatedText == null || generatedText.isBlank()) {
                return ResponseEntity.status(502).body(Map.of("error", "Gemini returned empty content"));
            }

            String planResponse;
            JsonNode whitelistNode;

            try {
                // The generatedText should be a JSON string because we asked for JSON
                JsonNode generatedJson = mapper.readTree(generatedText);
                planResponse = generatedJson.path("response").asText();
                whitelistNode = generatedJson.path("suggestedWhitelist");
            } catch (JsonProcessingException e) {
                // Fallback: return raw text if the model didn't return JSON
                planResponse = generatedText;
                whitelistNode = mapper.createArrayNode();
            }

            String whitelistStr = whitelistNode.toString(); // Store as JSON string in DB

            // Persist to DB
            ProductivityPlanner record = new ProductivityPlanner(
                request.getEducationLevel(),
                request.getTimeAvailableHours(),
                request.getSessionLength(),
                request.getPeakFocusTime(),
                subjectsText,
                methodsText,
                request.getWorkRestCycle(),
                request.getDistractionTolerance()
            );
            record.setResponse(planResponse);
            record.setSuggestedWhitelist(whitelistStr);

            try {
                ProductivityPlanner saved = repository.save(record);
                return ResponseEntity.ok(
                    Map.of(
                        "id", saved.getId(),
                        "response", planResponse,
                        "suggestedWhitelist", mapper.convertValue(whitelistNode, List.class)
                    )
                );
            } catch (Exception e) {
                // If persistence fails, still return the generated plan.
                return ResponseEntity.ok(
                    Map.of(
                        "response", planResponse,
                        "suggestedWhitelist", mapper.convertValue(whitelistNode, List.class)
                    )
                );
            }

        } catch (HttpClientErrorException.TooManyRequests e) {
            return ResponseEntity.ok(buildFallbackPlan(request));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.ok(buildFallbackPlan(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(buildFallbackPlan(request));
        }
    }

    private Map<String, Object> buildFallbackPlan(PlanRequest request) {
        // Local fallback so the extension works even if Gemini is unavailable.
        String educationLevel = request == null || request.getEducationLevel() == null ? "Unknown" : request.getEducationLevel();
        int timeAvailable = request == null ? 0 : request.getTimeAvailableHours();
        int sessionLength = request == null ? 25 : request.getSessionLength();
        String peakFocus = request == null || request.getPeakFocusTime() == null ? "Anytime" : request.getPeakFocusTime();
        String subjects = request == null || request.getSubjects() == null ? "" : String.join(", ", request.getSubjects());
        String methods = request == null || request.getLearningMethods() == null ? "" : String.join(", ", request.getLearningMethods());
        String workRest = request == null || request.getWorkRestCycle() == null ? "Flexible" : request.getWorkRestCycle();
        String distraction = request == null || request.getDistractionTolerance() == null ? "Medium" : request.getDistractionTolerance();
        String plan = String.format(
            "Study Plan (%s)\n" +
            "- Time available: %d hour(s)\n" +
            "- Session length: %d minutes\n" +
            "- Peak focus: %s\n" +
            "- Subjects: %s\n" +
            "- Methods: %s\n" +
            "- Work/rest: %s\n" +
            "- Distraction tolerance: %s\n\n" +
            "Plan:\n" +
            "1) Warm-up (5 min): review goals and materials.\n" +
            "2) Focus block(s): %d-minute sessions on top priority topic.\n" +
            "3) Active recall: self-quiz or summarize notes after each block.\n" +
            "4) Short breaks: stand up, hydrate, reset.\n" +
            "5) Wrap-up (5 min): capture what to do next." ,
            educationLevel,
            timeAvailable,
            sessionLength,
            peakFocus,
            subjects,
            methods,
            workRest,
            distraction,
            sessionLength
        );

        List<String> suggested = List.of(
            "wikipedia.org",
            "khanacademy.org",
            "coursera.org",
            "edx.org",
            "github.com"
        );

        return Map.of(
            "response", plan,
            "suggestedWhitelist", suggested
        );
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
