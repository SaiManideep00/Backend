package com.messagingservice.backendservice.controller.provider;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.messagingservice.backendservice.dto.provider.EventGroup;
import com.messagingservice.backendservice.dto.provider.EventsDTO;
import com.messagingservice.backendservice.model.provider.Connections;
import com.messagingservice.backendservice.model.provider.Events;
import com.messagingservice.backendservice.model.provider.Producer;
import com.messagingservice.backendservice.services.provider.ProviderService;
import com.mysql.cj.log.Log;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/")
//@CrossOrigin(origins = "http://prodeucerconsumerfrontend.s3-website-us-east-1.amazonaws.com/")
@CrossOrigin("*")
@Slf4j
@XRayEnabled
public class ProviderController {
    private final ProviderService providerService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Application is running";
    }
    @RequestMapping(path = "add/provider", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> addProvider(@RequestBody Producer producer){
        return providerService.saveProducer(producer);
    }

    @GetMapping("get/providers")
    public ResponseEntity<Object> getProviders(){
        log.info("Input Payload: {}");
//        return providerService.getProducers();
        ResponseEntity<Object> responseEntity = providerService.getProducers();
        log.info("Output Payload: {}", responseEntity);
        return responseEntity;

    }

    @Observed(name = "get.providers")
    @GetMapping("get/provider/{id}")
    public ResponseEntity<Object> getProviderById(@PathVariable Long id){
        System.out.println(id);
        log.info("Input Payload : {}",id);

        ResponseEntity<Object> responseEntity = providerService.getProducerById(id);


        log.info("Output Payload: {}", responseEntity.getBody());
        return providerService.getProducerById(id);
    }

    @GetMapping("get/provider/events/{providerId}")
    public ResponseEntity<Object> getEventsOfProvider(@PathVariable Long providerId){
        log.info("Input Payload : {}", providerId);

        ResponseEntity<Object> responseEntity = providerService.getEventsByProducerId(providerId);

        log.info("Output Payload : {}", responseEntity.getBody());

        return responseEntity;
    }

    @GetMapping("get/provider/connections/{providerId}")
    public ResponseEntity<Object> getConnectionsOfProvider(@PathVariable Long providerId){
        return providerService.getConnectionsByProducerId(providerId);
    }

    @GetMapping("get/provider/event/{id}")
    public ResponseEntity<Object> getEventById(@PathVariable Long id){
        log.info("Input Payload: {}", id);

        ResponseEntity<Object> responseEntity = providerService.getEventById(id);
        log.info("Output Payload: {}",responseEntity.getBody());
        return responseEntity;
    }

    @PutMapping("update/provider")
    public ResponseEntity<Object> updateProvider(@RequestBody Producer producer){
        log.info("Input Payload : {}", producer);
        ResponseEntity<Object> responseEntity = providerService.updateProducer(producer);
        log.info("Output Payload: {}", responseEntity.getBody());
        return responseEntity;
    }

    @PutMapping("add/event/provider/{providerId}")
    public ResponseEntity<Object> addEvent(@PathVariable long providerId, @RequestPart("file") MultipartFile file, @RequestPart("event") Events events){
        log.info("Input Payload : {}", providerId, file.getName(), events.getEventName());
        ResponseEntity<Object> responseEntity = providerService.addEventwithFile(providerId, events, file);
        log.info("Output Payload : {} ",responseEntity.getBody());
        return responseEntity;
    }

//    @PutMapping("/add/event/provider/{providerId}")
//    public ResponseEntity<Object> addEvent(@PathVariable long providerId, @RequestBody EventsDTO events){
//        return providerService.addEvent(providerId, events);
//    }

    @PutMapping("/add/event_group/provider/{providerId}")
    public ResponseEntity<Object> addEventGroup(@PathVariable long providerId, @RequestBody EventGroup events){
        log.info("Input Payload : {}", providerId, events);
        ResponseEntity<Object> responseEntity = providerService.addEventGroup(providerId, events);
        log.info("Output Payload : {}", responseEntity.getBody());
        return responseEntity;
    }

    @PutMapping("/update/provider/event/{eventId}")
    public ResponseEntity<Object> updateEvent(@PathVariable long eventId, @RequestBody Events events){
        log.info("Input Payload : {}", eventId, events);
        ResponseEntity<Object> responseEntity =providerService.updateEvent(eventId, events);
        log.info("Output Payload : {}", responseEntity.getBody());
        return responseEntity;
    }

    @PutMapping("/update/connection/{connectionId}")
    public ResponseEntity<Object> updateConnection(@PathVariable long connectionId, @RequestBody Connections connection){
        log.info("Input Payload : {}", connectionId,connection);
        ResponseEntity<Object> responseEntity = providerService.updateConnection(connectionId, connection);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.updateConnection(connectionId, connection);
    }

    @PutMapping("/add/connection/{providerId}")
    public ResponseEntity<Object> addConnection(@PathVariable long providerId, @RequestBody Connections connection){
        log.info("Input Payload : {}", providerId,connection);
        ResponseEntity<Object> responseEntity = providerService.addConnection(providerId, connection);
        log.info("Output Payload : {}",responseEntity.getBody());
        return providerService.addConnection(providerId, connection);
    }

    @PutMapping("/activate/provider/{providerId}")
    public ResponseEntity<Object> activate(@PathVariable int providerId){
        log.info("Input Payload: {}",providerId);
        ResponseEntity<Object> responseEntity = providerService.activateProvider(providerId);
        log.info("Output Payload: {}",responseEntity.getBody());
        return providerService.activateProvider(providerId);
    }

    @PutMapping("/suspend/provider/{providerId}")
    public ResponseEntity<Object> suspend(@PathVariable long providerId){
        log.info("Input payload: {}", providerId);
        ResponseEntity<Object> responseEntity =providerService.suspendProvider(providerId);
        log.info("Output payload : {}", responseEntity.getBody());
        return providerService.suspendProvider(providerId);
    }
    @PutMapping("/activate/event/{eventId}")
    public ResponseEntity<Object> activateEvent(@PathVariable long eventId){
        log.info("Input Payload : {}", eventId);
        ResponseEntity<Object> responseEntity =providerService.activateEvent(eventId);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.activateEvent(eventId);
    }
    @PutMapping("/suspend/event/{eventId}")
    public ResponseEntity<Object> suspendEvent(@PathVariable long eventId){
        log.info("Input Payload : {} ", eventId);
        ResponseEntity<Object> responseEntity =providerService.suspendEvent(eventId);
        log.info("Output payload : {}",responseEntity.getBody());
        return providerService.suspendEvent(eventId);
    }
    @PutMapping("/activate/connection/{connectionId}")
    public ResponseEntity<Object> activateConnection(@PathVariable long connectionId){
        log.info("Input payload : {}", connectionId);
        ResponseEntity<Object> responseEntity =providerService.activateConnection(connectionId);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.activateConnection(connectionId);
    }
    @PutMapping("/suspend/connection/{connectionId}")
    public ResponseEntity<Object> suspendConnection(@PathVariable long connectionId){
        log.info("Input Payload : {}",connectionId);
        ResponseEntity<Object> responseEntity = providerService.suspendConnection(connectionId);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.suspendConnection(connectionId);
    }
    @DeleteMapping("/delete/provider/event/{eventId}")
    public String deleteEvent(@PathVariable long eventId){
        log.info("Input Payload : {}", eventId);
        String str = providerService.deleteEvent(eventId);
        log.info("Output Payload : {}", str);
        return providerService.deleteEvent(eventId);
    }

    @DeleteMapping("/delete/provider/{id}")
    public ResponseEntity<Object> deleteProvider(@PathVariable Long id){
        log.info("Input Payload : {}",id);
        ResponseEntity<Object> responseEntity = providerService.deleteProducer(id);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.deleteProducer(id);
    }
    @GetMapping("/get/filters/{eventId}")
    public ResponseEntity<Object> getFilterKeys(@PathVariable long eventId){
        log.info("Input Payload : {}", eventId);
        ResponseEntity<Object> responseEntity = providerService.getFiltersByEventId(eventId);
        log.info("Output Payload : {}", responseEntity.getBody());
        return providerService.getFiltersByEventId(eventId);
    }
}
