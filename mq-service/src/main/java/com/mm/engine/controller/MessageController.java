package com.mm.engine.controller;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@XRayEnabled
@Slf4j

public class MessageController {

   public static String traceId;
   @Autowired
    private  RabbitTemplate rabbitTemplate;

   @Autowired
    private  AmqpAdmin amqpAdmin;
    public MessageController()
    {

    }

    public MessageController(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
        this.rabbitTemplate = rabbitTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> sendMessage(@RequestBody Object message, @RequestParam("exchange") String exchange, @RequestParam("routingKey") String routingKey, @RequestHeader(name = "X-Amzn-Trace-Id") String traceIdHeader){
        traceId=traceIdHeader;
        Gson gson = new Gson();
        String jsonBody = gson.toJson(message);
//        if(amqpAdmin.get)
        Subsegment subsegment = AWSXRay.beginSubsegment("AmazonMQ");
        try {
            subsegment.putAnnotation("Exchange", exchange);
            subsegment.putAnnotation("RoutingKey", routingKey);
            subsegment.putMetadata("MessageBody", message);

            rabbitTemplate.convertAndSend(exchange, routingKey, jsonBody.getBytes());
            log.info("Message sent to RabbitMQ..." + jsonBody);

            return ResponseEntity.ok("Message sent to RabbitMQ...");
        } catch (Exception e) {
            subsegment.addException(e);
            throw e;
        } finally {
            AWSXRay.endSubsegment();
        }


    }
}
