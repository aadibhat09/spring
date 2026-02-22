// package com.open.spring.mvc.productivity;

// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;

// import org.hibernate.annotations.JdbcTypeCode;
// import org.hibernate.type.SqlTypes;

// import com.fasterxml.jackson.annotation.JsonFormat;
// import com.open.spring.mvc.person.Person;

// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.JoinColumn;
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.Temporal;
// import jakarta.persistence.TemporalType;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;

// /**
//  * ProductivityData entity for Bud-E Chrome Extension
//  * 
//  * Stores user productivity tracking data including:
//  * - Whitelist of productive websites
//  * - Growth percentage of productivity pet
//  * - Timestamp tracking
//  * - User association
//  */
// @Data
// @AllArgsConstructor
// @NoArgsConstructor
// @Entity
// public class ProductivityData {

//     @Id
//     @GeneratedValue(strategy = GenerationType.AUTO)
//     private Long id;

//     /**
//      * Reference to the Person (user) who owns this productivity data
//      */
//     @ManyToOne
//     @JoinColumn(name = "person_id", nullable = false)
//     private Person person;

//     /**
//      * List of whitelisted productive websites
//      * Stored as JSON array
//      */
//     @JdbcTypeCode(SqlTypes.JSON)
//     @Column(columnDefinition = "TEXT", nullable = false)
//     private List<String> whitelist = new ArrayList<>();

//     /**
//      * Current growth percentage (0-100)
//      * Represents the productivity pet's growth level
//      */
//     @Column(nullable = false)
//     private Double growthPercent = 0.0;

//     /**
//      * Last time the growth was updated
//      */
//     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//     @Temporal(TemporalType.TIMESTAMP)
//     @Column(nullable = false)
//     private Date lastUpdateTime = new Date();

//     /**
//      * Timestamp when this record was created
//      */
//     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//     @Temporal(TemporalType.TIMESTAMP)
//     @Column(nullable = false, updatable = false)
//     private Date createdAt = new Date();

//     /**
//      * Timestamp when this record was last modified
//      */
//     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//     @Temporal(TemporalType.TIMESTAMP)
//     @Column(nullable = false)
//     private Date updatedAt = new Date();

//     /**
//      * AI-generated productivity suggestions (optional)
//      * Stored as text, can be updated periodically
//      */
//     @Column(columnDefinition = "TEXT")
//     private String suggestions;

//     /**
//      * Total productive time in seconds
//      * Accumulated when user is on whitelisted sites
//      */
//     @Column(nullable = false)
//     private Long totalProductiveTime = 0L;

//     /**
//      * Total sessions/visits tracked
//      */
//     @Column(nullable = false)
//     private Integer totalSessions = 0;

//     /**
//      * Highest growth percentage achieved
//      */
//     @Column(nullable = false)
//     private Double maxGrowthAchieved = 0.0;

//     /**
//      * Display name for leaderboard (optional)
//      * Defaults to user's name if not set
//      */
//     @Column(length = 100)
//     private String displayName;

//     /**
//      * Whether to show on public leaderboard
//      */
//     @Column(nullable = false)
//     private Boolean publicLeaderboard = true;
// }
