package com.example.insightpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class PrometheusGrafanaDashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrometheusGrafanaDashboardApplication.class, args);
	}

}
