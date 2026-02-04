package com.Sehaty.Sehaty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Sehaty Spring Boot application.
 * <p>
 * This class bootstraps the application and enables scheduling.
 * </p>
 */
@EnableScheduling
@SpringBootApplication
@EnableCaching
public class SehatyApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(SehatyApplication.class, args);
	}

}
