package com.xion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.xion.backend.config.IRASProperties"})

public class SalesTaxLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesTaxLibraryApplication.class, args);
	}

}
