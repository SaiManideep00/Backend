package com.mm.engine;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class MQServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MQServiceApplication.class, args);
	}
}
