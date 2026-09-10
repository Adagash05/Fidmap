package com.amsal.fidmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FidmapApplication {

	public static void main(String[] args) {
		SpringApplication.run(FidmapApplication.class, args);
	}

}
