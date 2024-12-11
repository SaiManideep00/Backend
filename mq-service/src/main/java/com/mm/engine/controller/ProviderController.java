package com.mm.engine.controller;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("api/provider")
@XRayEnabled
@Slf4j
public class ProviderController {
    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;

    private final SimpleMessageListenerContainer container;
    private final WebClient.Builder webClientBuilder;


    public ProviderController(ConnectionFactory connectionFactory, RabbitAdmin rabbitAdmin, SimpleMessageListenerContainer container, WebClient.Builder webClientBuilder) {
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = rabbitAdmin;
        this.container = container;
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/create/exchange")
    public String createExchange(@RequestParam String exchangeName) {
        // Declare exchange

        rabbitAdmin.declareExchange(new FanoutExchange(exchangeName));
        log.info("Exchange created successfully."+exchangeName);
        return "Exchange created successfully.";
    }

    @GetMapping("test")
    public void test() throws URISyntaxException {
        String baseUrl = "http://backend-service:9191/api/test";
        URIBuilder uriBuilder = new URIBuilder(baseUrl);
        URI uri = uriBuilder.build();

        ResponseEntity<String>  response = webClientBuilder.build().get()
                .uri(uri)
                .retrieve().toEntity(String.class)
                .block();
        Segment segment = AWSXRay.getCurrentSegmentOptional().orElse(null);
        String traceId=null;
        if(segment!=null)
            traceId=segment.getTraceId().toString();
        System.out.println("Trace ID sent is "+traceId);
        System.out.println("Received from backend "+response);

    }
}
