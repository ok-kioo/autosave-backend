package com.signature.autosave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

@SpringBootApplication
public class AutosaveApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(AutosaveApplication.class);

		app.setApplicationStartup(
				new BufferingApplicationStartup(2048)
		);

		app.run(args);
	}
}