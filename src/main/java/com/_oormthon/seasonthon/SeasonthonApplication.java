package com._oormthon.seasonthon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SeasonthonApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeasonthonApplication.class, args);
	}

}
