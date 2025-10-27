package com.Sehaty.Sehaty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SehatyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SehatyApplication.class, args);
	}

}
