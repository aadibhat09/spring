// package com.open.spring.mvc.productivity;

// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import java.util.stream.Collectors;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;

// import com.open.spring.mvc.person.Person;
// import com.open.spring.mvc.person.PersonJpaRepository;

// import lombok.Data;

// /**
//  * ProductivityApiController for Bud-E Chrome Extension
//  * 
//  * REST API endpoints for managing user productivity data
//  * 
//  * Endpoints:
//  * - GET /api/productivity/data - Get current productivity data
//  * - POST /api/productivity/data - Create or initialize productivity data
//  * - PUT /api/productivity/data - Update productivity data
//  * - PUT /api/productivity/whitelist - Update whitelist
//  * - PUT /api/productivity/growth - Update growth percentage
//  * - PUT /api/productivity/settings - Update leaderboard settings
//  * - GET /api/productivity/leaderboard - Get leaderboard (optional sortBy param)
//  * - DELETE /api/productivity/data - Delete productivity data
//  */
// @RestController
// @RequestMapping("/api/productivity")
// public class ProductivityApiController {

//     @Autowired
//     private ProductivityRepository productivityRepository;

//     @Autowired
//     private PersonJpaRepository personRepository;

//     /**
//      * Get current user's productivity data
//      * 
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with productivity data or error
//      */
//     @GetMapping("/data")
//     public ResponseEntity<?> getProductivityData(Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             Optional<ProductivityData> dataOpt = productivityRepository.findByPerson(person);
            
//             if (dataOpt.isEmpty()) {
//                 // Return default empty data if none exists
//                 Map<String, Object> defaultData = new HashMap<>();
//                 defaultData.put("whitelist", List.of());
//                 defaultData.put("growthPercent", 0.0);
//                 defaultData.put("lastUpdateTime", new Date());
//                 defaultData.put("suggestions", null);
//                 return ResponseEntity.ok(defaultData);
//             }

//             ProductivityData data = dataOpt.get();
//             Map<String, Object> response = new HashMap<>();
//             response.put("id", data.getId());
//             response.put("whitelist", data.getWhitelist());
//             response.put("growthPercent", data.getGrowthPercent());
//             response.put("lastUpdateTime", data.getLastUpdateTime());
//             response.put("suggestions", data.getSuggestions());

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to retrieve productivity data: " + e.getMessage()));
//         }
//     }

//     /**
//      * Create or initialize productivity data for current user
//      * 
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with created data or error
//      */
//     @PostMapping("/data")
//     public ResponseEntity<?> createProductivityData(Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             // Check if data already exists
//             if (productivityRepository.existsByPerson(person)) {
//                 return ResponseEntity.status(HttpStatus.CONFLICT)
//                         .body(Map.of("error", "Productivity data already exists for this user"));
//             }

//             ProductivityData data = new ProductivityData();
//             data.setPerson(person);
//             data.setWhitelist(List.of());
//             data.setGrowthPercent(0.0);
//             data.setLastUpdateTime(new Date());
//             data.setCreatedAt(new Date());
//             data.setUpdatedAt(new Date());

//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("id", savedData.getId());
//             response.put("whitelist", savedData.getWhitelist());
//             response.put("growthPercent", savedData.getGrowthPercent());
//             response.put("lastUpdateTime", savedData.getLastUpdateTime());
//             response.put("message", "Productivity data created successfully");

//             return ResponseEntity.status(HttpStatus.CREATED).body(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to create productivity data: " + e.getMessage()));
//         }
//     }

//     /**
//      * Update productivity data (full update)
//      * 
//      * @param request ProductivityUpdateRequest with all fields
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with updated data or error
//      */
//     @PutMapping("/data")
//     public ResponseEntity<?> updateProductivityData(
//             @RequestBody ProductivityUpdateRequest request,
//             Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             ProductivityData data = productivityRepository.findByPerson(person)
//                     .orElseGet(() -> {
//                         ProductivityData newData = new ProductivityData();
//                         newData.setPerson(person);
//                         newData.setCreatedAt(new Date());
//                         return newData;
//                     });

//             if (request.getWhitelist() != null) {
//                 data.setWhitelist(request.getWhitelist());
//             }
//             if (request.getGrowthPercent() != null) {
//                 data.setGrowthPercent(request.getGrowthPercent());
//             }
//             data.setLastUpdateTime(new Date());
//             data.setUpdatedAt(new Date());

//             if (request.getSuggestions() != null) {
//                 data.setSuggestions(request.getSuggestions());
//             }

//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("id", savedData.getId());
//             response.put("whitelist", savedData.getWhitelist());
//             response.put("growthPercent", savedData.getGrowthPercent());
//             response.put("lastUpdateTime", savedData.getLastUpdateTime());
//             response.put("suggestions", savedData.getSuggestions());
//             response.put("message", "Productivity data updated successfully");

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to update productivity data: " + e.getMessage()));
//         }
//     }

//     /**
//      * Update only the whitelist
//      * 
//      * @param request WhitelistUpdateRequest with whitelist
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with updated data or error
//      */
//     @PutMapping("/whitelist")
//     public ResponseEntity<?> updateWhitelist(
//             @RequestBody WhitelistUpdateRequest request,
//             Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             ProductivityData data = productivityRepository.findByPerson(person)
//                     .orElseGet(() -> {
//                         ProductivityData newData = new ProductivityData();
//                         newData.setPerson(person);
//                         newData.setCreatedAt(new Date());
//                         newData.setGrowthPercent(0.0);
//                         return newData;
//                     });

//             data.setWhitelist(request.getWhitelist());
//             data.setUpdatedAt(new Date());

//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("whitelist", savedData.getWhitelist());
//             response.put("message", "Whitelist updated successfully");

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to update whitelist: " + e.getMessage()));
//         }
//     }

//     /**
//      * Update only the growth percentage
//      * 
//      * @param request GrowthUpdateRequest with growth percentage
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with updated data or error
//      */
//     @PutMapping("/growth")
//     public ResponseEntity<?> updateGrowth(
//             @RequestBody GrowthUpdateRequest request,
//             Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             ProductivityData data = productivityRepository.findByPerson(person)
//                     .orElseGet(() -> {
//                         ProductivityData newData = new ProductivityData();
//                         newData.setPerson(person);
//                         newData.setCreatedAt(new Date());
//                         newData.setWhitelist(List.of());
//                         return newData;
//                     });

//             data.setGrowthPercent(request.getGrowthPercent());
//             data.setLastUpdateTime(new Date());
//             data.setUpdatedAt(new Date());

//             // Track max growth achieved
//             if (request.getGrowthPercent() > data.getMaxGrowthAchieved()) {
//                 data.setMaxGrowthAchieved(request.getGrowthPercent());
//             }

//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("growthPercent", savedData.getGrowthPercent());
//             response.put("lastUpdateTime", savedData.getLastUpdateTime());
//             response.put("maxGrowthAchieved", savedData.getMaxGrowthAchieved());
//             response.put("message", "Growth percentage updated successfully");

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to update growth: " + e.getMessage()));
//         }
//     }

//     /**
//      * Update leaderboard settings (display name and public visibility)
//      * 
//      * @param request LeaderboardSettingsRequest with settings
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with success message or error
//      */
//     @PutMapping("/settings")
//     public ResponseEntity<?> updateSettings(
//             @RequestBody LeaderboardSettingsRequest request,
//             Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             ProductivityData data = productivityRepository.findByPerson(person)
//                     .orElseGet(() -> {
//                         ProductivityData newData = new ProductivityData();
//                         newData.setPerson(person);
//                         newData.setCreatedAt(new Date());
//                         newData.setWhitelist(List.of());
//                         newData.setGrowthPercent(0.0);
//                         return newData;
//                     });

//             if (request.getDisplayName() != null) {
//                 data.setDisplayName(request.getDisplayName());
//             }
//             if (request.getPublicLeaderboard() != null) {
//                 data.setPublicLeaderboard(request.getPublicLeaderboard());
//             }
//             data.setUpdatedAt(new Date());

//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("displayName", savedData.getDisplayName());
//             response.put("publicLeaderboard", savedData.getPublicLeaderboard());
//             response.put("message", "Settings updated successfully");

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to update settings: " + e.getMessage()));
//         }
//     }

//     /**
//      * Get productivity leaderboard
//      * 
//      * @param sortBy Sort criteria: "current" (default), "max", or "time"
//      * @param limit Number of entries to return (default 10, max 100)
//      * @return ResponseEntity with leaderboard data or error
//      */
//     @GetMapping("/leaderboard")
//     public ResponseEntity<?> getLeaderboard(
//             @RequestParam(defaultValue = "current") String sortBy,
//             @RequestParam(defaultValue = "10") Integer limit) {
//         try {
//             // Validate and cap limit
//             if (limit < 1) limit = 10;
//             if (limit > 100) limit = 100;

//             Pageable pageable = PageRequest.of(0, limit);
//             List<ProductivityData> leaderboardData;

//             switch (sortBy.toLowerCase()) {
//                 case "max":
//                     leaderboardData = productivityRepository.findTopByMaxGrowth(pageable);
//                     break;
//                 case "time":
//                     leaderboardData = productivityRepository.findTopByProductiveTime(pageable);
//                     break;
//                 case "current":
//                 default:
//                     leaderboardData = productivityRepository.findTopByGrowthPercent(pageable);
//                     break;
//             }

//             List<Map<String, Object>> leaderboard = new ArrayList<>();
//             int rank = 1;
//             for (ProductivityData data : leaderboardData) {
//                 Map<String, Object> entry = new HashMap<>();
//                 entry.put("rank", rank++);
                
//                 // Use display name if set, otherwise use person's name
//                 String displayName = data.getDisplayName();
//                 if (displayName == null || displayName.trim().isEmpty()) {
//                     displayName = data.getPerson().getName();
//                 }
//                 entry.put("displayName", displayName);
                
//                 entry.put("growthPercent", data.getGrowthPercent());
//                 entry.put("maxGrowthAchieved", data.getMaxGrowthAchieved());
//                 entry.put("totalProductiveTime", data.getTotalProductiveTime());
//                 entry.put("totalSessions", data.getTotalSessions());
                
//                 leaderboard.add(entry);
//             }

//             Map<String, Object> response = new HashMap<>();
//             response.put("leaderboard", leaderboard);
//             response.put("sortBy", sortBy);
//             response.put("total", leaderboard.size());

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to retrieve leaderboard: " + e.getMessage()));
//         }
//     }

//     /**
//      * Update productivity stats (for tracking sessions and time)
//      * 
//      * @param request ProductivityStatsRequest with time and session data
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with success message or error
//      */
//     @PutMapping("/stats")
//     public ResponseEntity<?> updateStats(
//             @RequestBody ProductivityStatsRequest request,
//             Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             ProductivityData data = productivityRepository.findByPerson(person)
//                     .orElseGet(() -> {
//                         ProductivityData newData = new ProductivityData();
//                         newData.setPerson(person);
//                         newData.setCreatedAt(new Date());
//                         newData.setWhitelist(List.of());
//                         newData.setGrowthPercent(0.0);
//                         return newData;
//                     });

//             // Update productive time
//             if (request.getProductiveTimeSeconds() != null && request.getProductiveTimeSeconds() > 0) {
//                 data.setTotalProductiveTime(data.getTotalProductiveTime() + request.getProductiveTimeSeconds());
//             }

//             // Increment session counter
//             if (request.getIncrementSession() != null && request.getIncrementSession()) {
//                 data.setTotalSessions(data.getTotalSessions() + 1);
//             }

//             data.setUpdatedAt(new Date());
//             ProductivityData savedData = productivityRepository.save(data);

//             Map<String, Object> response = new HashMap<>();
//             response.put("totalProductiveTime", savedData.getTotalProductiveTime());
//             response.put("totalSessions", savedData.getTotalSessions());
//             response.put("message", "Stats updated successfully");

//             return ResponseEntity.ok(response);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to update stats: " + e.getMessage()));
//         }
//     }

//     /**
//      * Delete productivity data for current user
//      * 
//      * @param authentication Spring Security authentication
//      * @return ResponseEntity with success message or error
//      */
//     @DeleteMapping("/data")
//     public ResponseEntity<?> deleteProductivityData(Authentication authentication) {
//         try {
//             Person person = getAuthenticatedPerson(authentication);
//             if (person == null) {
//                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                         .body(Map.of("error", "User not authenticated"));
//             }

//             Optional<ProductivityData> dataOpt = productivityRepository.findByPerson(person);
//             if (dataOpt.isEmpty()) {
//                 return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                         .body(Map.of("error", "No productivity data found for this user"));
//             }

//             productivityRepository.delete(dataOpt.get());

//             return ResponseEntity.ok(Map.of("message", "Productivity data deleted successfully"));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Failed to delete productivity data: " + e.getMessage()));
//         }
//     }

//     /**
//      * Helper method to get authenticated person from Spring Security context
//      */
//     private Person getAuthenticatedPerson(Authentication authentication) {
//         if (authentication == null || !authentication.isAuthenticated()) {
//             return null;
//         }

//         String email = authentication.getName();
//         Optional<Person> personOpt = personRepository.findByEmail(email);
//         return personOpt.orElse(null);
//     }

//     // ==================== Request DTOs ====================

//     @Data
//     public static class ProductivityUpdateRequest {
//         private List<String> whitelist;
//         private Double growthPercent;
//         private String suggestions;
//     }

//     @Data
//     public static class WhitelistUpdateRequest {
//         private List<String> whitelist;
//     }

//     @Data
//     public static class GrowthUpdateRequest {
//         private Double growthPercent;
//     }

//     @Data
//     public static class LeaderboardSettingsRequest {
//         private String displayName;
//         private Boolean publicLeaderboard;
//     }

//     @Data
//     public static class ProductivityStatsRequest {
//         private Long productiveTimeSeconds;
//         private Boolean incrementSession;
//     }
// }
