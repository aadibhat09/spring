package com.open.spring.mvc.productivity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.open.spring.mvc.person.Person;

/**
 * ProductivityRepository for Bud-E Chrome Extension
 * 
 * Handles database operations for productivity data
 */
@Repository
public interface ProductivityRepository extends JpaRepository<ProductivityData, Long> {
    
    /**
     * Find productivity data by person
     * @param person The person/user
     * @return Optional containing the productivity data if found
     */
    Optional<ProductivityData> findByPerson(Person person);
    
    /**
     * Check if productivity data exists for a person
     * @param person The person/user
     * @return true if data exists, false otherwise
     */
    boolean existsByPerson(Person person);
    
    /**
     * Find top users by current growth percentage
     * Only includes users who opted into public leaderboard
     * @param pageable Pagination parameters
     * @return List of productivity data ordered by growth percentage
     */
    @Query("SELECT p FROM ProductivityData p WHERE p.publicLeaderboard = true ORDER BY p.growthPercent DESC")
    List<ProductivityData> findTopByGrowthPercent(Pageable pageable);
    
    /**
     * Find top users by max growth achieved
     * @param pageable Pagination parameters
     * @return List of productivity data ordered by max growth achieved
     */
    @Query("SELECT p FROM ProductivityData p WHERE p.publicLeaderboard = true ORDER BY p.maxGrowthAchieved DESC")
    List<ProductivityData> findTopByMaxGrowth(Pageable pageable);
    
    /**
     * Find top users by total productive time
     * @param pageable Pagination parameters
     * @return List of productivity data ordered by total productive time
     */
    @Query("SELECT p FROM ProductivityData p WHERE p.publicLeaderboard = true ORDER BY p.totalProductiveTime DESC")
    List<ProductivityData> findTopByProductiveTime(Pageable pageable);
}
