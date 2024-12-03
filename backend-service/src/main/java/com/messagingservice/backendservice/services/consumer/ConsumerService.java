package com.messagingservice.backendservice.services.consumer;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.messagingservice.backendservice.dto.consumer.AlertSubscriptionDTO;
import com.messagingservice.backendservice.dto.consumer.ConsumerBasicDetailsDTO;
import com.messagingservice.backendservice.dto.consumer.SubscribedEventsGroup;
import com.messagingservice.backendservice.exception.ResourceNotFoundException;
import com.messagingservice.backendservice.mapper.ConsumerMapper;
import com.messagingservice.backendservice.model.consumer.*;
import com.messagingservice.backendservice.model.provider.Events;
import com.messagingservice.backendservice.repository.consumer.*;
import com.messagingservice.backendservice.repository.provider.ConnectionsRepository;
import com.messagingservice.backendservice.repository.provider.EventsRepository;
import com.messagingservice.backendservice.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@XRayEnabled
@Slf4j
public class ConsumerService {
    private final ConsumerRepository consumerRepository;
    private final SubscribedEventsRepository subscribedEventsRepository;
    private final SubscribedEventConnectionsRepository subscribedEventConnectionsRepository;
    private final EventsRepository eventsRepository;
    private final ConnectionsRepository connectionsRepository;
    private final ConsumerMapper consumerMapper = Mappers.getMapper(ConsumerMapper.class);
    private final WebClient.Builder webClientBuilder;
    private final Gson gson;
    private final AlertCriteriaRepository alertCriteriaRepository;
    private final RecepientGroupRepository recepientGroupRepository;
    private final DBWatchlistRepository dbWatchlistRepository;

    private final AlertSubscriptionRepository alertSubscriptionRepository;

    public ResponseEntity<Object> saveConsumer(Consumer consumer){
        ConsumerBasicDetailsDTO consumerDTO = consumerMapper.toConsumerBasicDetailsDTO(consumerRepository.save(consumer));
        //createConsumerQueue(consumer.getConsumerName());
        ResponseEntity<Object> responseEntity = Util.prepareResponse(consumerDTO, HttpStatus.CREATED);
        log.info("Saved consumer "+consumer+" to the database");
        return responseEntity;
    }

    public void createConsumerQueue(String queueName){
        String baseUrl = "http://albformqservice-515380053.us-east-2.elb.amazonaws.com/create/listener";

        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);

            // Create the URI object from the built URL
            URI uri = uriBuilder.build();
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("queueName", queueName);
            String jsonBody = gson.toJson(requestBody);
            String str = webClientBuilder.build().post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(str);
            log.info("Created consumer queue with name "+str);
            // Process the response as needed
//            int statusCode = response.statusCode();
        } catch (URISyntaxException e) {
            log.error("Cannot create consumer queue");
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Object> getConsumers(){
        List<Consumer> consumers = new ArrayList<>();
        consumerRepository.findAll().forEach(consumers::add);
        List<ConsumerBasicDetailsDTO> consumerList = consumerMapper.toConsumerBasicDetailsDTO(consumers);
        log.info("Fetched consumers  "+consumerList);
        ResponseEntity<Object> responseEntity = Util.prepareResponse(consumerList, HttpStatus.CREATED);
        return responseEntity;

    }

    public ResponseEntity<Object> getConsumerById(long id){
        ResponseEntity<Object> responseEntity;

        Optional<Consumer> consumer = consumerRepository.findById(id);
        if(consumer.isPresent()){
            ConsumerBasicDetailsDTO consumerDTO = consumerMapper.toConsumerBasicDetailsDTO(consumer.get());
            log.info("Fetched consumer with id "+id+consumerDTO);
            responseEntity = Util.prepareResponse(consumerDTO, HttpStatus.OK);
        }
        else {
            log.error("Cannot find consumer with id "+id);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ id
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> getConsumerEventsByConsumerId(long id){
        Set<SubscribedEvents> subscribedEvents;
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(id);
        if(consumer.isPresent()) {
            subscribedEvents = consumer.get().getSubscribedEvents();
            log.info("Fetched events of the consumer with id "+id+subscribedEvents);
            responseEntity = Util.prepareResponse(subscribedEvents, HttpStatus.OK);
        }
        else{
            log.error("Cannot find consumer with id "+id);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ id
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> getConsumerAlertsByConsumerId(Long consumerId) {
        Set<AlertSubscription> alertSubscriptions;
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(consumerId);
        if(consumer.isPresent()) {
            alertSubscriptions = consumer.get().getAlertSubscriptions();
            responseEntity = Util.prepareResponse(alertSubscriptions, HttpStatus.OK);
            log.info("Fetched alerts of the consumer with id "+consumerId+ alertSubscriptions);
        }
        else{
            log.error("Cannot find consumer with id "+consumerId);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ consumerId
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> getSubscribedEventById(long id){
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEvents> subscribedEvent = subscribedEventsRepository.findById(id);
        if(subscribedEvent.isPresent()){
            responseEntity = Util.prepareResponse(subscribedEvent.get(), HttpStatus.OK);
            log.info("Fetched subscribed event with id "+id+subscribedEvent);}
        else{
            log.error("Cannot find subscribed event with Id "+ id);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested subscribed event with Id "+ id
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> subscribeEventsByConsumerId(long id, SubscribedEvents subscribedEvents) {
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(id);
        Optional<SubscribedEventConnections> connections;
//        alertNotificationsCriteriaRepository.save(subscribedEvents.getAlertNotificationsCriteria());
        if(!eventsRepository.findById(subscribedEvents.getEventId()).isPresent()){
            log.error("Cannot find the event with id "+subscribedEvents.getEventId());
            responseEntity = Util.prepareErrorResponse("404", "Sorry the event with Id "+ subscribedEvents.getEventId()
                    +" does not exist", HttpStatus.NOT_FOUND);
            return responseEntity;
        }
        if (consumer.isPresent()) {
            if(subscribedEvents.getSubscribedEventConnections().getConnectionId() != null){
                connections = subscribedEventConnectionsRepository.
                        findById(subscribedEvents.getSubscribedEventConnections().getConnectionId());
                if(connections.isPresent()) {
                    subscribedEvents.setSubscribedEventConnections(connections.get());
                    subscribedEvents.setEventName(eventsRepository.findById(subscribedEvents.getEventId()).get().getEventName());
                }
                else{
                    log.error("Cannot find connection with Id "+ id);
                    responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with Id "+ id
                            +" does not exist", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            else{
                if(subscribedEvents.getSubscribedEventConnections() != null)
                    consumer.get().addConnections(subscribedEvents.getSubscribedEventConnections());
                //consumerRepository.save(consumer.get());
                //subscribedEventConnectionsRepository.save(subscribedEvents.getSubscribedEventConnections());
                //subscribedEvents.setSubscribedEventConnections(connections);
            }
            Optional<Events> event = eventsRepository.findById(subscribedEvents.getEventId());
            subscribedEvents.setEventName(event.get().getEventName());
            subscribedEvents.setSourceDataFormat(event.get().getDataFormat());
            //subscribedEventsRepository.save(subscribedEvents);
            consumer.get().addEvent(subscribedEvents);
            //**** Un comment later *****
            String exchangeName = new StringBuilder().append(subscribedEvents.getProviderName())
                    .append(".")
                    .append(eventsRepository.findById(subscribedEvents.getEventId()).get().getEventName()).toString();
            subscribeToExchange(consumer.get().getConsumerName()+".event."+eventsRepository.findById(subscribedEvents.getEventId()).get().getEventName(), exchangeName);
            responseEntity = Util.prepareResponse(consumerRepository.save(consumer.get()).getSubscribedEvents(), HttpStatus.OK);
            log.info("Fetched subscribed events with id "+id);
        }
        else{
            log.error("Cannot find consumer with id "+id);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ id
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public void subscribeToExchange(String queueName, String exchangeName){
        String baseUrl = "http://albformqservice-515380053.us-east-2.elb.amazonaws.com/bind";

        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            JsonObject requestBody = new JsonObject();
            System.out.println(exchangeName);
            System.out.println(exchangeName.split(".",-2)[0]);
            requestBody.addProperty("queueName", queueName);
            requestBody.addProperty("exchangeName", exchangeName);
            String jsonBody = gson.toJson(requestBody);
            String str = webClientBuilder.build().post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Subscribed to Exchange "+str);
            System.out.println(str);
        } catch (URISyntaxException e) {
            log.error("cannot subscribe to exchange");
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Object> unsubscribeEventsBySubscriptionId(long subscriptionId) {
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEvents> subscribedEvent = subscribedEventsRepository.findById(subscriptionId);
        if (subscribedEvent.isPresent()) {
//            subscribedEvent.get().setSubscribedEventConnections(null);
//            subscribedEventsRepository.save(subscribedEvent.get());
            Optional<Consumer> consumer = subscribedEventsRepository.findConsumerBySubscriptionId(subscriptionId);
            subscribedEventsRepository.deleteById(subscriptionId);
            //un comment below line later
            unSubscribeToExchange(consumer.get().getConsumerName()+".event."+subscribedEvent.get().getEventName(), subscribedEvent.get().getProviderName()+"."+subscribedEvent.get().getEventName());
            log.info("Unsubscribed to event with id "+subscriptionId);
            responseEntity = Util.prepareResponse(subscribedEvent, HttpStatus.OK);
        }
        else{
            log.error("Cannot find the requested event with Id "+ subscriptionId);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id "+ subscriptionId
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    public void unSubscribeToExchange(String queueName, String exchangeName){
        String baseUrl = "http://albformqservice-515380053.us-east-2.elb.amazonaws.com/unbind";
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            JsonObject requestBody = new JsonObject();
            System.out.println(exchangeName);
            System.out.println(exchangeName.split(".",-2)[0]);
            requestBody.addProperty("queueName", queueName);
            requestBody.addProperty("exchangeName", exchangeName);
            String jsonBody = gson.toJson(requestBody);
            String response = webClientBuilder.build().put()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            System.out.println(response);
            log.info("Unsubscribed to exchange "+response);
            // Process the response as needed
        } catch (URISyntaxException e) {
            log.error("Cannot unsubscribe to the exchange");
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Object> subscribeEventsGroupByConsumerId(long id, SubscribedEventsGroup subscribedEventsGroup) {
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(id);
        SubscribedEventConnections connections;
//        if(subscribedEventsGroup.getSubscribedEventConnections().getConnectionId() != null){
//            connections = subscribedEventConnectionsRepository.findById(subscribedEventsGroup.getSubscribedEventConnections().getConnectionId()).get();
//            subscribedEventsGroup.setSubscribedEventConnections(connections);
//        }
//        else{
//            String connectionName = subscribedEventsGroup.getSubscribedEventConnections().getConnectionName();
//            subscribedEventConnectionsRepository.save(subscribedEventsGroup.getSubscribedEventConnections());
//            connections = subscribedEventConnectionsRepository.findByConnectionName(connectionName).get();
//        }
        if(subscribedEventsGroup.getSubscribedEventConnections().getConnectionId() != null){
            connections = subscribedEventConnectionsRepository.findById(subscribedEventsGroup.getSubscribedEventConnections().getConnectionId()).get();
            subscribedEventsGroup.setSubscribedEventConnections(connections);
        }
        else{
            String connectionName = subscribedEventsGroup.getSubscribedEventConnections().getConnectionName();
            consumer.get().addConnections(subscribedEventsGroup.getSubscribedEventConnections());
            connections = subscribedEventConnectionsRepository.findByConnectionName(id, connectionName).get();
        }
        if (consumer.isPresent()) {
            for(int i=0; i<subscribedEventsGroup.getEventIds().size(); i++){
                SubscribedEvents subscribedEvents = new SubscribedEvents();
                Long eventid = subscribedEventsGroup.getEventIds().get(i);
                Optional<Events> event = eventsRepository.findById(eventid);
                if(event.isPresent()) {
                    subscribedEvents.setEventName(event.get().getEventName());
                    subscribedEvents.setEventId(subscribedEventsGroup.getEventIds().get(i));
                    subscribedEvents.setSubscribedEventConnections(connections);
                    subscribedEvents.setProviderName(subscribedEventsGroup.getProviderName());
                    subscribedEvents.setDataFormat(subscribedEventsGroup.getDataFormat());
                    consumer.get().addEvent(subscribedEvents);
                }
                else{
                    log.error("Cannot find the requested event with Id "+ id);
                    responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id "+ id
                            +" does not exist", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            responseEntity = Util.prepareResponse(consumerRepository.save(consumer.get()).getSubscribedEvents(), HttpStatus.OK);
            log.info("Subscribed events group for consumer with id "+id+responseEntity.getBody());
        }
        else{
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ id
                    +" does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find the requested consumer with Id "+ id);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> updateSubscribedEvent(long id, SubscribedEvents subscribedEvents){
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEvents> existingEvent = subscribedEventsRepository.findById(id);
        Optional<Consumer> consumer = subscribedEventsRepository.findConsumerBySubscriptionId(id);

        if(!existingEvent.isPresent()){
            log.error("Cannot find Subscription with the Id " + id );
            return Util.prepareErrorResponse("404" , "Subscription with the Id " + id + "does mot exist", HttpStatus.NOT_FOUND);
        }
        if(subscribedEvents.getSubscribedEventConnections() == null){
            log.error("Subscription should have at least one connection");
            return Util.prepareErrorResponse("404" , "Subscription should have at least one connection", HttpStatus.NOT_FOUND);
        }
        if(subscribedEvents.getSubscribedEventConnections().getConnectionId() == null){
            SubscribedEventConnections subscribedEventConnections = subscribedEvents.getSubscribedEventConnections();
            consumer.get().addConnections(subscribedEventConnections);
            subscribedEventConnectionsRepository.save(subscribedEventConnections);
            existingEvent.get().setSubscribedEventConnections(subscribedEventConnections);
            existingEvent.get().setDataFormat(subscribedEvents.getDataFormat());
            existingEvent.get().setFilters(subscribedEvents.getFilters());
        }
        else{
            Long connectionId = subscribedEvents.getSubscribedEventConnections().getConnectionId();
            Optional<SubscribedEventConnections> subscribedEventConnections = subscribedEventConnectionsRepository
                    .findById(connectionId);
            if(subscribedEventConnections.isPresent()){
                existingEvent.get().setSubscribedEventConnections(subscribedEventConnections.get());
                existingEvent.get().setDataFormat(subscribedEvents.getDataFormat());
                existingEvent.get().setFilters(subscribedEvents.getFilters());
            }
            else {
                log.error("Cannot find the requested connection with id "+ connectionId);
                return Util.prepareErrorResponse("404", "Sorry the requested connection with id "+ connectionId
                        +" does not exist", HttpStatus.NOT_FOUND);
            }
        }
        log.info("updated subscribed event for id "+id);
        return Util.prepareResponse(subscribedEventsRepository.save(existingEvent.get()), HttpStatus.OK);
    }

    public String deleteEvent(long id){
        if(subscribedEventsRepository.findById(id) != null) {
            subscribedEventsRepository.deleteById(id);
            log.info("Event with id "+id+" deleted successfully");
            return "Event with id "+id+" deleted successfully";
        }
        else{
            log.error("Cannot find the event with id "+id);
            throw new ResourceNotFoundException("Event not found details with id = " + id);
        }
    }

    public String deleteConsumerEvent(long id){
        if(subscribedEventsRepository.findById(id) != null) {
            subscribedEventsRepository.deleteById(id);
            log.info("Event with id "+id+" deleted successfully");
            return "Event with id "+id+" deleted successfully";
        }
        else{log.error("Cannot find the event with id "+id);

            throw new ResourceNotFoundException("No Event found with id = " + id);
        }
    }

    public ResponseEntity<Object> updateConsumer(Consumer consumer){
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> existingConsumer = consumerRepository.findByConsumerName(consumer.getConsumerName());
        if(existingConsumer.isPresent()) {
            existingConsumer.get().setConsumerTechnicalPOC(consumer.getConsumerTechnicalPOC());
            existingConsumer.get().setConsumerBusinessPOC(consumer.getConsumerBusinessPOC());
            existingConsumer.get().setAlertNotificationEmailID(consumer.getAlertNotificationEmailID());
            ConsumerBasicDetailsDTO consumerDTO = consumerMapper.toConsumerBasicDetailsDTO(existingConsumer.get());
            responseEntity = Util.prepareResponse(consumerDTO, HttpStatus.OK);
            log.info("Updated consumer with details ");
            consumerRepository.save(existingConsumer.get());
        }
        else{
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with name "+ consumer.getConsumerName()
                    +" does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find requested consumer");
        }

        return responseEntity;
    }
    public ResponseEntity<Object> updateConnection(long connectionId, SubscribedEventConnections connection) {
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEventConnections> existingConnection = subscribedEventConnectionsRepository.findById(connectionId);
        if (existingConnection.isPresent()) {
            existingConnection.get().setConnectionType(connection.getConnectionType());
            existingConnection.get().setUrl(connection.getUrl());
            existingConnection.get().setUsername(connection.getUsername());
            existingConnection.get().setPassword(connection.getPassword());
            responseEntity = Util.prepareResponse(subscribedEventConnectionsRepository.save(existingConnection.get()), HttpStatus.OK);
            log.info("Updated connection with id "+connectionId);
        }
        else{
            log.error("cannot find the requested connection with id "+ connectionId);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with id "+ connectionId
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
    public ResponseEntity<Object> activateConsumer(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(id);
        if(consumer.isPresent()) {
            consumer.get().setActive(true);
            consumerRepository.save(consumer.get());
            responseEntity = Util.prepareResponse(consumer, HttpStatus.OK);
            log.info("Activated consumer with id "+id);
        }
        else{
            log.error("Cannot find consumer with id "+id);
            responseEntity = Util.prepareErrorResponse( "400","The consumer with id "+id+" is not present", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;

    }

    public ResponseEntity<Object> suspendConsumer(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(id);
        if(consumer.isPresent()) {
            consumer.get().setActive(false);
            consumerRepository.save(consumer.get());
            log.info("Suspended consumer with id "+id);
            responseEntity = Util.prepareResponse(consumer, HttpStatus.OK);
        }
        else{
            log.error("Cannot find consumer with id "+id);
            responseEntity = Util.prepareErrorResponse( "400","The consumer with id "+id+" is not present", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }
    public ResponseEntity<Object> activateSubscribedEvent(long id){
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEvents> subscribedEvent = subscribedEventsRepository.findById(id);
        if(subscribedEvent.isPresent()) {
            subscribedEvent.get().setActive(true);
            subscribedEventsRepository.save(subscribedEvent.get());
            listenerController(subscribedEventsRepository.findConsumerBySubscriptionId(id).get().getConsumerName() + ".event." +subscribedEvent.get().getEventName(), "http://mq-service/start/listener");
            responseEntity = Util.prepareResponse(subscribedEvent, HttpStatus.OK);
            log.info("Activated subscribed event with id "+id);
        }
        else{
            log.error("Cannot find Subscribed Event with id "+id);
            responseEntity = Util.prepareErrorResponse( "400","The Subscribed Event with id "+id+" is not present", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    public void listenerController(String queueName, String baseUrl){

        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("queueName", queueName);
            String jsonBody = gson.toJson(requestBody);
            String response = webClientBuilder.build().put()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            log.info("Created listener "+response);
            System.out.println(response);
            // Process the response as needed
        } catch (URISyntaxException e) {
            log.error("Cannot create listener");
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Object> suspendSubscribedEvent(long id){
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEvents> subscribedEvent = subscribedEventsRepository.findById(id);
        if(subscribedEvent.isPresent()) {
            subscribedEvent.get().setActive(false);
            subscribedEventsRepository.save(subscribedEvent.get());
            listenerController(subscribedEventsRepository.findConsumerBySubscriptionId(id).get().getConsumerName() + ".event." +subscribedEvent.get().getEventName(), "http://mq-service/stop/listener");
            responseEntity = Util.prepareResponse(subscribedEvent, HttpStatus.OK);
            log.info("Suspended subscribed event with id "+id);
        }
        else{
            log.error("Cannot find  Subscribed Event with id "+id);
            responseEntity = Util.prepareErrorResponse( "400","The Subscribed Event with id "+id+" is not present", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> suspendEventSubscription(String eventName, String consumerName, String providerName){
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findByConsumerName(consumerName);
        if(!consumer.isPresent()){
            log.error("Cannot find consumer with name "+consumerName);
            return Util.prepareErrorResponse("404", "Consumer with name "+consumerName+" is not found", HttpStatus.NOT_FOUND);
        }

        Optional<SubscribedEvents> subscribedEvent = subscribedEventsRepository.findByConsumerIdAndEventNameAndProviderName(consumer.get().getConsumerId(), eventName, providerName);
        if(subscribedEvent.isPresent()) {
            subscribedEvent.get().setActive(false);
            subscribedEventsRepository.save(subscribedEvent.get());
            log.info("Suspended Event with name "+eventName);
//            listenerController(subscribedEventsRepository.findConsumerBySubscriptionId(id).get().getConsumerName() + ".event." +subscribedEvent.get().getEventName(), "http://mq-service/stop/listener");
            responseEntity = Util.prepareResponse(subscribedEvent, HttpStatus.OK);
        }
        else{
            log.error("Cannot find Subscribed Event with provided details ");
            responseEntity = Util.prepareErrorResponse( "400","The Subscribed Event with provided details is not present", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }


    public String deleteConsumer(long id){
        if(getConsumerById(id)!=null) {
            consumerRepository.deleteById(id);
            log.info("Consumer with id " + id + " deleted.");
            return "Consumer with id " + id + " deleted.";
        }
        log.error("No Consumer exits with id "+id);
        return "No Consumer exits with id "+id;
    }

    public ResponseEntity<Object> getConsumerConnectionsById(Long consumerId) {
        List<SubscribedEventConnections> subscribedEventConnections;
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(consumerId);
        if(consumer.isPresent()) {
            subscribedEventConnections = consumer.get().getConnections();
            responseEntity = Util.prepareResponse(subscribedEventConnections, HttpStatus.OK);
            log.info("Fetched consumer connections with id "+consumerId);
        }
        else {
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id " + consumerId
                    + " does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find the consumer with id " + consumerId);
        }

        return responseEntity;

    }

    public ResponseEntity<Object> activateConnection(long connectionId) {
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEventConnections> connection = subscribedEventConnectionsRepository.findById(connectionId);
        if(connection.isPresent()) {
            if (!connection.get().isActive()) {
                connection.get().setActive(true);
                subscribedEventConnectionsRepository.save(connection.get());
            }
            log.info("Activated Connection with id "+connectionId);
            responseEntity = Util.prepareResponse(connection, HttpStatus.OK);
        }
        else {responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer connection with Id " + connectionId
                + " does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find the requested consumer connection with Id " + connectionId);}
        return responseEntity;
    }

    public ResponseEntity<Object> suspendConnection(long connectionId) {
        ResponseEntity<Object> responseEntity;
        Optional<SubscribedEventConnections> connection = subscribedEventConnectionsRepository.findById(connectionId);
        if(connection.isPresent()) {
            if (connection.get().isActive()) {
                connection.get().setActive(false);
                subscribedEventConnectionsRepository.save(connection.get());
                log.info("Suspended connection with id "+connectionId);
            }
            responseEntity = Util.prepareResponse(connection, HttpStatus.OK);
        }
        else{
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer connection with Id " + connectionId
                    + " does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find requested consumer connection with Id" + connectionId);
        }

        return responseEntity;
    }

    public ResponseEntity<Object> getConnectionOfSubscribedEvent(String providerName, String eventName, String consumerName) {
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findByConsumerName(consumerName);
        Long consumerId = consumer.get().getConsumerId();
        Optional<SubscribedEvents> subscribedEvents = subscribedEventsRepository.findByConsumerIdAndEventNameAndProviderName(consumerId, eventName, providerName);
        if(subscribedEvents.isPresent()){
            SubscribedEventConnections subscribedEventConnections = subscribedEvents.get().getSubscribedEventConnections();
            responseEntity = Util.prepareResponse(subscribedEventConnections, HttpStatus.OK);
            log.info("Fetched connections of subscribed events "+responseEntity.getBody());
            return responseEntity;
        }
        log.error("Cannot find the requested event connection");
        return responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event connection"
                + " does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> getSubscribedEvent(String providerName, String eventName, String consumerName) {
        Optional<Consumer> consumer = consumerRepository.findByConsumerName(consumerName);
        Long consumerId = consumer.get().getConsumerId();
        Optional<SubscribedEvents> subscribedEvents = subscribedEventsRepository.findByConsumerIdAndEventNameAndProviderName(consumerId, eventName, providerName);
        if(subscribedEvents.isPresent()){
            log.info("Fetched subscribed event for provider "+providerName);
            return Util.prepareResponse(subscribedEvents.get(), HttpStatus.OK);
        }
        log.error("Cannot find requested subscription");
        return Util.prepareErrorResponse("404", "Sorry the requested subscription"
                + " does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> getSubscribedAlert(String providerName, String eventName, String consumerName) {
        Optional<Consumer> consumer = consumerRepository.findByConsumerName(consumerName);
        Long consumerId = consumer.get().getConsumerId();
        Optional<AlertSubscription> alertSubscription = alertSubscriptionRepository.findByConsumerIdAndEventNameAndProviderName(consumerId, eventName, providerName);
        if(alertSubscription.isPresent()){
            log.info("Fetched subscribed alert for "+providerName);
            return Util.prepareResponse(alertSubscription.get(), HttpStatus.OK);
        }
        log.error("Cannot find the requested subscription");
        return Util.prepareErrorResponse("404", "Sorry the requested subscription"
                + " does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> subscribeAlertsByConsumerId(long consumerId, AlertSubscriptionDTO alertSubscriptionDTO) {
        ResponseEntity<Object> responseEntity;
        Optional<Consumer> consumer = consumerRepository.findById(consumerId);
        Optional<AlertCriteria> alertCriteria;
//        alertNotificationsCriteriaRepository.save(subscribedEvents.getAlertNotificationsCriteria());
        Optional<Events> event = eventsRepository.findById(alertSubscriptionDTO.getEventId());
        if(!event.isPresent()){
            responseEntity = Util.prepareErrorResponse("404", "Sorry the event with Id "+ alertSubscriptionDTO.getEventId()
                    +" does not exist", HttpStatus.NOT_FOUND);
            log.error("Cannot find event with Id "+ alertSubscriptionDTO.getEventId());
            return responseEntity;
        }
        if (consumer.isPresent()) {
            if(alertSubscriptionDTO.getAlertCriteria() != null)
                alertSubscriptionDTO.setEventName(eventsRepository.findById(alertSubscriptionDTO.getEventId()).get().getEventName());//subscribedEventsRepository.save(subscribedEvents);
            else
            {
                log.error("Alert Criteria is missing, please try again by adding alert criteria");
                return Util.prepareErrorResponse("400", "Alert Criteria is missing, please try again by adding alert criteria", HttpStatus.BAD_REQUEST);

            }

            if(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup() == null){
                log.error("Alert Criteria must have a Recipient");
                return Util.prepareErrorResponse("400", "Alert Criteria must have a Recipient", HttpStatus.BAD_REQUEST);
            }
            if(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId() == null){
                RecepientGroup recepientGroup = recepientGroupRepository.save(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup());
                alertSubscriptionDTO.getAlertCriteria().setRecepientGroup(recepientGroup);
                log.info("Saved alert");
            }
            else{
                Optional<RecepientGroup> recepientGroup = recepientGroupRepository.findById(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId());
                if(recepientGroup.isPresent()){
                    alertSubscriptionDTO.getAlertCriteria().setRecepientGroup(recepientGroup.get());
                    log.info("Get Alert");
                }
                else {
                    log.error("Cannot find Recepient Group with the id "+ alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId());
                    return Util.prepareErrorResponse("400", "Sorry the Recepient Group with the id "+ alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId() +" is not present", HttpStatus.BAD_REQUEST);
                }
            }
            alertCriteriaRepository.save(alertSubscriptionDTO.getAlertCriteria());
            List<Long> dbConnectionIds = alertSubscriptionDTO.getDbWatchlistIds();
            AlertSubscription alertSubscription = new AlertSubscription();
            alertSubscription.setAlertCriteria(alertSubscriptionDTO.getAlertCriteria());
            alertSubscription.setEventId(alertSubscriptionDTO.getEventId());
            alertSubscription.setEventName(alertSubscriptionDTO.getEventName());
            alertSubscription.setProviderName(alertSubscriptionDTO.getProviderName());
            alertSubscription.setSourceDataFormat(event.get().getDataFormat());
            alertSubscription = alertSubscriptionRepository.save(alertSubscription);
            for(long connectionId : dbConnectionIds){
                Optional<DBWatchlist> dbConnection = dbWatchlistRepository.findById(connectionId);
                if(dbConnection.isPresent()) {
                    alertSubscription.addWatchlist(dbConnection.get());
                    alertSubscriptionRepository.save(alertSubscription);
//                    dbWatchlistRepository.save(dbConnection.get());
                }
                else
                {
                    log.error("Cannot find Connection with Id "+connectionId);
                    return Util.prepareErrorResponse("400", "Connection with Id "+connectionId + " does not exist", HttpStatus.NOT_FOUND);
                }

            }

            consumer.get().addAlert(alertSubscription);
            responseEntity = Util.prepareResponse(consumerRepository.save(consumer.get()).getAlertSubscriptions(), HttpStatus.OK);
            log.info("Saved consumer alert");
            //**** Un comment later *****
            String exchangeName = new StringBuilder().append(alertSubscription.getProviderName())
                    .append(".")
                    .append(eventsRepository.findById(alertSubscription.getEventId()).get().getEventName()).toString();
            subscribeToExchange(consumer.get().getConsumerName()+".alert."+eventsRepository.findById(alertSubscriptionDTO.getEventId()).get().getEventName(), exchangeName);
        }
        else{
            log.error("cannot find the consumer with id "+consumerId);
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested consumer with Id "+ consumerId
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
    public ResponseEntity<Object> saveDBConnection(DBWatchlist dbWatchlist) {
        log.info("Saved DBWatchList "+dbWatchlist);
        return Util.prepareResponse(dbWatchlistRepository.save(dbWatchlist), HttpStatus.CREATED);
    }

    public ResponseEntity<Object> updateAlertSubscription(long id, AlertSubscriptionDTO alertSubscriptionDTO){
        ResponseEntity<Object> responseEntity;
        Optional<AlertSubscription> existingAlert = alertSubscriptionRepository.findById(id);
        Optional<Consumer> consumer = subscribedEventsRepository.findConsumerBySubscriptionId(id);

        if(!existingAlert.isPresent()){
            log.error("Alert Subscription with the Id " + id + "does mot exist");
            return Util.prepareErrorResponse("404" , "Alert Subscription with the Id " + id + "does mot exist", HttpStatus.NOT_FOUND);
        }

        if(alertSubscriptionDTO.getAlertCriteria() == null) {
            log.error("Alert Criteria is missing, please try again by adding alert criteria");
            return Util.prepareErrorResponse("400", "Alert Criteria is missing, please try again by adding alert criteria", HttpStatus.BAD_REQUEST);
        }
        if(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup() == null){
            log.error("Alert Criteria must have a Recipient");
            return Util.prepareErrorResponse("400", "Alert Criteria must have a Recipient", HttpStatus.BAD_REQUEST);
        }
        if(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId() == null){
            RecepientGroup recepientGroup = recepientGroupRepository.save(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup());
            alertSubscriptionDTO.getAlertCriteria().setRecepientGroup(recepientGroup);
            log.info("Saved recepient group");
        }
        else{
            Optional<RecepientGroup> recepientGroup = recepientGroupRepository.findById(alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId());
            if(recepientGroup.isPresent()){
                alertSubscriptionDTO.getAlertCriteria().setRecepientGroup(recepientGroup.get());
                log.info("fetched alert criteria");
            }
            else {
                log.error("Cannot find Recipient Group with the id "+ alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId());
                return Util.prepareErrorResponse("400", "Sorry the Recipient Group with the id "+ alertSubscriptionDTO.getAlertCriteria().getRecepientGroup().getGroupId() +" is not present", HttpStatus.BAD_REQUEST);
            }
        }
        alertCriteriaRepository.save(alertSubscriptionDTO.getAlertCriteria());
        log.info("Saved alert subscription");

        List<Long> dbConnectionIds = alertSubscriptionDTO.getDbWatchlistIds();
        existingAlert.get().setAlertCriteria(alertSubscriptionDTO.getAlertCriteria());
        Set<DBWatchlist> dbWatchlist = new HashSet<DBWatchlist>();
        existingAlert.get().setWatchlists(dbWatchlist);
        for(long connectionId : dbConnectionIds){
            Optional<DBWatchlist> dbConnection = dbWatchlistRepository.findById(connectionId);
            if(dbConnection.isPresent()) {
                existingAlert.get().addWatchlist(dbConnection.get());
            }
            else {
                log.error("Cannot find Connection with Id " + connectionId);
                return Util.prepareErrorResponse("400", "Connection with Id " + connectionId + " does not exist", HttpStatus.NOT_FOUND);
            }
        }
        log.info("Saved alert subscription");
        return Util.prepareResponse(alertSubscriptionRepository.save(existingAlert.get()), HttpStatus.OK);
    }

    public ResponseEntity<Object> addDBConnectionToAlertSubscription(Long alertId, DBWatchlist dbWatchlistRequest) {

        AlertSubscription _alertSubscription = alertSubscriptionRepository.findById(alertId).map(alertSubscription -> {
            Long dbId = dbWatchlistRequest.getWatchlistId();

            // tag is existed
            if (dbId != null) {
                DBWatchlist _dbWatchlist = dbWatchlistRepository.findById(dbId)
                        .orElseThrow(() -> new ResourceNotFoundException("Not found DB connection with id = " + dbId));
                ;
                alertSubscriptionRepository.save(alertSubscription);
                log.info("Saved alert subscription");
                return alertSubscription;
            }

            // add and create new Tag
            alertSubscription.addWatchlist(dbWatchlistRequest);
            dbWatchlistRepository.save(dbWatchlistRequest);
            return alertSubscription;
        }).orElseThrow(() -> new ResourceNotFoundException("Not found subscription with id = " + alertId));

        return new ResponseEntity<>(_alertSubscription, HttpStatus.CREATED);
    }

    public ResponseEntity<Object> getDBWatchlist() {
        List<DBWatchlist> dbWatchlists = dbWatchlistRepository.findAll().stream().collect(Collectors.toList());
        log.info("Fetched DBWatch list "+dbWatchlists);
        return Util.prepareResponse(dbWatchlists,HttpStatus.OK);
    }

    public ResponseEntity<Object> getRecipientList() {
        List<RecepientGroup> recipientGroups = recepientGroupRepository.findAll().stream().collect(Collectors.toList());
        log.info("Fetched Recipient List "+recipientGroups);
        return Util.prepareResponse(recipientGroups, HttpStatus.OK);
    }

    public ResponseEntity<Object> saveRecipientGroup(RecepientGroup recepientGroup) {
        log.info("Saved RecipientGroup "+recepientGroup);
        return Util.prepareResponse(recepientGroupRepository.save(recepientGroup),HttpStatus.CREATED);
    }

    public ResponseEntity<Object> suspendAlertSubscriptionById(Long alertId) {
        Optional<AlertSubscription> alertSubscription = alertSubscriptionRepository.findById(alertId);
        if(alertSubscription.isPresent()){
            if(alertSubscription.get().isActive()) {
                alertSubscription.get().setActive(false);
                ResponseEntity<Object> responseEntity = Util.prepareResponse(alertSubscriptionRepository.save(alertSubscription.get()), HttpStatus.OK);
                listenerController(alertSubscriptionRepository.findConsumerByAlertId(alertId).get().getConsumerName() + ".alert." +alertSubscription.get().getEventName(), "http://mq-service/stop/listener");
                log.info("suspended alert with id "+alertId);
                return responseEntity;
            }
        }
        log.error("Cannot find Alert subscription with id "+ alertId);
        return Util.prepareErrorResponse("404", "Sorry the requested Alert subscription with id "+ alertId +" does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> suspendAlertSubscription(String eventName, String consumerName, String providerName) {
        Optional<Consumer> consumer = consumerRepository.findByConsumerName(consumerName);
        if(!consumer.isPresent()) {
            log.error("Cannot find Consumer with name " + consumerName);
            return Util.prepareErrorResponse("404", "Consumer with name " + consumerName + " is not found", HttpStatus.NOT_FOUND);
        }
        Optional<AlertSubscription> alertSubscription = alertSubscriptionRepository.findByConsumerIdAndEventNameAndProviderName(consumer.get().getConsumerId(), eventName, providerName);
        if(alertSubscription.isPresent()){
            if(alertSubscription.get().isActive()) {
                alertSubscription.get().setActive(false);
                ResponseEntity<Object> responseEntity = Util.prepareResponse(alertSubscriptionRepository.save(alertSubscription.get()), HttpStatus.OK);
                log.info("Suspended Alert Subscription for "+consumerName);
                return responseEntity;
            }
        }
        log.error("Alert subscription with provided details does not exist");
        return Util.prepareErrorResponse("404", "Sorry the requested Alert subscription with provided details does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> activateAlertSubscription(Long alertId) {
        Optional<AlertSubscription> alertSubscription = alertSubscriptionRepository.findById(alertId);
        if(alertSubscription.isPresent()){
            if(!alertSubscription.get().isActive()) {
                alertSubscription.get().setActive(true);
                ResponseEntity<Object> responseEntity = Util.prepareResponse(alertSubscriptionRepository.save(alertSubscription.get()), HttpStatus.OK);
                listenerController(alertSubscriptionRepository.findConsumerByAlertId(alertId).get().getConsumerName() + ".alert." +alertSubscription.get().getEventName(), "http://mq-service/start/listener");
                log.info("Activated Alert with id "+alertId);
                return responseEntity;
            }
        }
        log.error("requested Alert subscription with id "+ alertId+"  does not exist");
        return Util.prepareErrorResponse("404", "Sorry the requested Alert subscription with id "+ alertId +" does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> deleteAlert(long alertId) {
        Optional<AlertSubscription> alertSubscription = alertSubscriptionRepository.findById(alertId);
        if(alertSubscription.isPresent()){
            alertSubscriptionRepository.deleteById(alertId);
            log.info("Unsubscribed to the event "+alertSubscription.get().getEventName() + " successfully");
            return Util.prepareResponse("Unsubscribed to the event "+alertSubscription.get().getEventName() + " successfully", HttpStatus.OK);
        }
        log.error("Cannot find Alert Subscription with the Id " + alertId);
        return Util.prepareErrorResponse("404" , "Alert Subscription with the Id " + alertId + "does mot exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> updateDBConnection(Long watchlistId, DBWatchlist dbWatchlist) {
        Optional<DBWatchlist> existingdbWatchlist = dbWatchlistRepository.findById(watchlistId);
        if(existingdbWatchlist.isPresent()){
            existingdbWatchlist.get().setConnectionType(dbWatchlist.getConnectionType());
            existingdbWatchlist.get().setUrl(dbWatchlist.getUrl());
            existingdbWatchlist.get().setUsername(dbWatchlist.getUsername());
            existingdbWatchlist.get().setPassword(dbWatchlist.getPassword());
            log.info("Updated DB Connection");
            return Util.prepareResponse(dbWatchlistRepository.save(existingdbWatchlist.get()),HttpStatus.OK);
        }
        log.error("Cannot find Connection with Id "+watchlistId);
        return Util.prepareErrorResponse("400", "Connection with Id "+watchlistId + " does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> updateRecipientGroup(Long groupId, RecepientGroup recepientGroup) {
        Optional<RecepientGroup> existingRecipientGroup = recepientGroupRepository.findById(groupId);
        if(existingRecipientGroup.isPresent()){
            existingRecipientGroup.get().setEmailIds(recepientGroup.getEmailIds());
            log.info("Updated Recipient Group with id "+groupId);
            return Util.prepareResponse(recepientGroupRepository.save(existingRecipientGroup.get()),HttpStatus.OK);
        }
        log.error("Cannot find Recipient Group with Id "+groupId );
        return Util.prepareErrorResponse("400", "Recipient Group with Id "+groupId + " does not exist", HttpStatus.NOT_FOUND);
    }
}
