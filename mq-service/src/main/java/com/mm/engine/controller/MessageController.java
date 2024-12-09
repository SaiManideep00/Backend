package com.mm.engine.controller;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@XRayEnabled
@Slf4j
public class MessageController {
    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    public MessageController(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestBody Object message, @RequestParam("exchange") String exchange, @RequestParam("routingKey") String routingKey){

        Gson gson = new Gson();
        String jsonBody = gson.toJson(message);
//        if(amqpAdmin.get)
        rabbitTemplate.convertAndSend(exchange, routingKey, jsonBody.getBytes());
        log.info("Message sent to RabbitMQ..."+jsonBody);
        return ResponseEntity.ok("Message sent to RabbitMQ...");
    }
}
