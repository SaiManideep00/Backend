package com.messagingservice.deliveryservice.controller;

import com.messagingservice.deliveryservice.service.DeliveryService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class DataFetcher {
    private final DeliveryService deliveryService;

    public DataFetcher(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @RequestMapping(path = "send/json/data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addProvider(@RequestHeader(name = "ProviderName") String providerName,
                                              @RequestHeader(name = "EventName") String eventName, @RequestBody Object data){
        return deliveryService.publishDataToMQ(providerName, eventName, data);
    }

    @RequestMapping(path = "send/xml/data", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> publishXMLData(@RequestHeader(name = "ProviderName") String providerName, @RequestHeader(name = "EventName") String eventName, @RequestBody Object data){
//        System.out.println(providerName);
//        System.out.println((String) data.toString());
        return deliveryService.publishDataToMQ(providerName, eventName, data);
    }
}
