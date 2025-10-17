package com.OfficeManagement.OfficeProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class OfficeProjectApplication extends SpringBootServletInitializer {

	// THIS METHOD IS CRITICAL FOR WAR DEPLOYMENT
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(OfficeProjectApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(OfficeProjectApplication.class, args);
	}
}