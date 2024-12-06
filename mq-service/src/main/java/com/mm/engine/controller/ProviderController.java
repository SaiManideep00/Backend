package com.mm.engine.controller;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/provider")
@XRayEnabled
@Slf4j
public class ProviderController {
    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;

    private final SimpleMessageListenerContainer container;

    public ProviderController(ConnectionFactory connectionFactory, RabbitAdmin rabbitAdmin, SimpleMessageListenerContainer container) {
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = rabbitAdmin;
        this.container = container;
    }

    @GetMapping("/create/exchange")
    public String createExchange(@RequestParam String exchangeName) {
        // Declare exchange

        rabbitAdmin.declareExchange(new FanoutExchange(exchangeName));
        log.info("Exchange created successfully."+exchangeName);
        return "Exchange created successfully.";
    }
}
