package com.messagingservice.deliveryservice.controller;

import com.messagingservice.deliveryservice.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    @RequestMapping("rest")
    public String deliverViaRestAPI(){
        return "";
    }

    @PostMapping("deliver")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<String> deliver(@RequestHeader String providerName, @RequestHeader String eventName,
                                          @RequestHeader String consumerName, @RequestHeader String subscriptionType, @RequestBody Object object){
        if(subscriptionType.equalsIgnoreCase("alert")){
            return deliveryService.notify(providerName, eventName, consumerName, object);
        }
        return deliveryService.identifyDeliveryMethodAndDeliver(providerName, eventName, consumerName, object);
    }

}
