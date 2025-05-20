package com.team901.CapstoneDesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.team901.CapstoneDesign.GPT")
public class CapstoneDesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapstoneDesignApplication.class, args);
	}

}
