package com.dbsight.neo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NeoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeoApplication.class, args);
	}

}
