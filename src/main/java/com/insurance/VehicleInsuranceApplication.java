package com.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Vehicle Insurance Management System.
 *
 * Enables:
 *  - JPA Auditing   → auto-populates createdAt / updatedAt fields
 *  - Scheduling     → policy expiry reminders
 *  - Async          → non-blocking email dispatch
 *  - Caching        → Redis-backed policy lookups
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
@EnableCaching
public class VehicleInsuranceApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleInsuranceApplication.class, args);
    }
}
