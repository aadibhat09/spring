package com.open.spring.mvc.productivityPlanner;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductivityPlannerRepository extends JpaRepository<ProductivityPlanner, Long> {
    // This interface is intentionally left blank. 
    // Default JPA methods are used for database operations.
}
