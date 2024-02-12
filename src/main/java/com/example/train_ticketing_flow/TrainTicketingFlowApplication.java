package com.example.train_ticketing_flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrainTicketingFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrainTicketingFlowApplication.class, args);
	}

}
