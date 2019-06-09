package com.example.taskconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TaskconsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskconsumerApplication.class, args);
	}

}
