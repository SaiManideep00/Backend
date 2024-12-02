package com.messagingservice.backendservice.services.provider;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.messagingservice.backendservice.dto.provider.EventGroup;
import com.messagingservice.backendservice.dto.provider.EventsDTO;
import com.messagingservice.backendservice.dto.provider.ProviderBasicDetailsDTO;
import com.messagingservice.backendservice.exception.ResourceNotFoundException;
import com.messagingservice.backendservice.mapper.ProviderMapper;
import com.messagingservice.backendservice.model.provider.Connections;
import com.messagingservice.backendservice.model.provider.Events;
import com.messagingservice.backendservice.model.provider.Producer;
import com.messagingservice.backendservice.repository.consumer.ConsumerRepository;
import com.messagingservice.backendservice.repository.consumer.SubscribedEventsRepository;
import com.messagingservice.backendservice.repository.provider.ConnectionsRepository;
import com.messagingservice.backendservice.repository.provider.EventsRepository;
import com.messagingservice.backendservice.repository.provider.ProducerRepository;
import com.messagingservice.backendservice.util.FileUtil;
import com.messagingservice.backendservice.util.JSONParser;
import com.messagingservice.backendservice.util.Util;
import com.messagingservice.backendservice.util.XMLParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProviderService {

    private final ProducerRepository producerRepository;
    private final EventsRepository eventsRepository;
    private final ConnectionsRepository connectionsRepository;
    private final ConsumerRepository consumerRepository;
    private final ProviderMapper providerMapper = Mappers.getMapper(ProviderMapper.class);
    private final SubscribedEventsRepository subscribedEventsRepository;
    private final WebClient.Builder webClientBuilder;
    private final Gson gson;

//    public Producer validateAndSaveProducer(Producer producer){
//        if(producerRepository.findByProviderName(producer.getProviderName()) == null){
//            return saveProducer(producer);
//        }
//        else{
//            throw new ResourceNotFoundException("Provider with name = " + producer.getProviderName() +
//                    " already exits" + "\n" + "please provide unique provider name." );
//        }
//    }

    public ResponseEntity<Object> saveProducer(Producer producer){
        ProviderBasicDetailsDTO providerDTO = providerMapper.toProviderBasicDetailsDTO(producerRepository.save(producer));
        log.info("[ProducerService : saveProducer(Post)] Saving Producer info: "+producer);
        ResponseEntity<Object> responseEntity = Util.prepareResponse(providerDTO, HttpStatus.CREATED);
        log.info("Successfully saved producer info: "+ responseEntity.getBody());
        return responseEntity;

    }

    public List<Producer> saveProducers(List<Producer> producers){
        return producerRepository.saveAll(producers);
    }

    public ResponseEntity<Object> getProducers(){
        List<Producer> producers = new ArrayList<Producer>();
        producerRepository.findAll().forEach(producers::add);
        List<ProviderBasicDetailsDTO> providerList = providerMapper.toProviderBasicDetailsDTO(producers);
        ResponseEntity<Object> responseEntity = Util.prepareResponse(providerList, HttpStatus.OK);
        return responseEntity;
    }

    public ResponseEntity<Object> getProducerById(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()) {
            ProviderBasicDetailsDTO providerDTO = providerMapper.toProviderBasicDetailsDTO(producer.get());
            responseEntity = Util.prepareResponse(providerDTO, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        //.orElseThrow(() -> new ResourceNotFoundException("Not found Provider with id = " + id))
        return responseEntity;
    }

    public ResponseEntity<Object> getEventById(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Events> event = eventsRepository.findById(id);
        if(event.isPresent()) {
            responseEntity = Util.prepareResponse(event.get(), HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        //.orElseThrow(() -> new ResourceNotFoundException("Not found Provider with id = " + id));
        return responseEntity;
    }

    public ResponseEntity<Object> getEventsByProducerId(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()){
            List<Events> connectionParameters = producer.get().getEvents();
            responseEntity = Util.prepareResponse(connectionParameters, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
//        Producer producer1 = (Producer) responseEntity.getBody();
        return responseEntity;
    }

    public ResponseEntity<Object> getConnectionsByProducerId(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()){
            List<Connections> connections = producer.get().getConnections();
            responseEntity = Util.prepareResponse(connections, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
//        Producer producer1 = (Producer) responseEntity.getBody();
        return responseEntity;
    }

    public boolean areConnectionsEqual(Connections newConnection, Connections existingConnection){
        if(newConnection.getConnectionName().equalsIgnoreCase(existingConnection.getConnectionName())){
            if(newConnection.getUrl().equalsIgnoreCase(existingConnection.getUrl())){
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<Set<String>> processFile(MultipartFile file){
        Set<String> filters = null;
        if(FileUtil.isJSONFile(file)){
            try {
                String jsonData = FileUtil.readJSONFile(file);
                Map<String, List<List<String>>> hashMap = JSONParser.jsonToMap(jsonData);
                filters = hashMap.keySet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (FileUtil.isXMLFile(file)) {
            try {
                String xmlData = FileUtil.readXMLFile(file);
                System.out.println(xmlData);
                Map<String, Object> hashmap = XMLParser.getKeys(file);
                System.out.println(hashmap);
                filters = hashmap.keySet();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            return Util.prepareResponse(null,HttpStatus.BAD_REQUEST);
        }
        return Util.prepareResponse(filters,HttpStatus.OK);
    }

    public ResponseEntity<Object> addEventwithFile(long id, Events events, MultipartFile file){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        ResponseEntity<Set<String>> processResponse = processFile(file);
        if(processResponse.getStatusCode().equals(HttpStatus.OK)){
            events.setFilters(processResponse.getBody());
        }
        else{
            return Util.prepareErrorResponse("400","Invalid file, please upload JSON/XML file", HttpStatus.BAD_REQUEST);
        }
        Connections connections = null;
        if(producer.isPresent()) {
            if(events.getEventType().equalsIgnoreCase("push")) events.setConnections(null);
            else if(events.getConnections().getConnectionId() != null){
                if(connectionsRepository.findById(events.getConnections().getConnectionId()).isPresent()) {
                    connections = connectionsRepository.findById(events.getConnections().getConnectionId()).get();
                    events.setConnections(connections);
                }
                else{
                    responseEntity = Util.prepareErrorResponse("404", "Sorry the connection with Id " + events.getConnections().getConnectionId()
                            + " does not exist", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            else {
                if (events.getConnections() != null){
                    //connections = connectionsRepository.save(events.getConnections());
                    producer.get().addConnections(events.getConnections());
                }
                else{
                    responseEntity = Util.prepareErrorResponse("404", "Please add connection details", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            Events _event = new Events();
            _event.setEventName(events.getEventName());
            _event.setEventType(events.getEventType());
            _event.setOrderOfEvents(events.getOrderOfEvents());
            _event.setFilters(events.getFilters());
            _event.setConnections(events.getConnections());
            _event.setDataFormat(events.getDataFormat());
            producer.get().addEvents(_event);
            producer = Optional.of(producerRepository.save(producer.get()));
            //Un comment later
            createExchange(producer.get().getProviderName()+"."+events.getEventName());
            responseEntity = Util.prepareResponse(producer.get().getEvents(), HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> addEvent(long id, EventsDTO events){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(events.getData() == null){
            return Util.prepareErrorResponse("400", "Data is null", HttpStatus.BAD_REQUEST);
        }
        Map<String, List<List<String>>> filterMap;
        if(events.getDataFormat().equalsIgnoreCase("json")) {
            System.out.println(events.getData());
            String jsonBody = gson.toJson(events.getData());
            System.out.println("json body: ");
            System.out.println(jsonBody);
            filterMap = JSONParser.jsonToMap(jsonBody);
            events.setFilters(filterMap.keySet());
            events.setDataFormat("json");
        }
        else{
            //System.out.println(events.getData().toString());
            XmlMapper xmlMapper = new XmlMapper();
            String xmlBody=(String) events.getData();
            System.out.println(xmlBody);
            filterMap = XMLParser.xmlToMap(xmlBody);
            events.setFilters(filterMap.keySet());
            events.setDataFormat("xml");
        }
//        String jsonBody = gson.toJson(events.getJsonData());
//        Map<String, Object> hashMap = JSONParser.jsonToMap(jsonBody);
//        events.setFilters(hashMap.keySet());

        Connections connections = null;
        if(producer.isPresent()) {
            if(events.getEventType().equalsIgnoreCase("push")) events.setConnections(null);
            else if(events.getConnections().getConnectionId() != null){
                if(connectionsRepository.findById(events.getConnections().getConnectionId()).isPresent()) {
                    connections = connectionsRepository.findById(events.getConnections().getConnectionId()).get();
                    events.setConnections(connections);
                }
                else{
                    responseEntity = Util.prepareErrorResponse("404", "Sorry the connection with Id " + events.getConnections().getConnectionId()
                            + " does not exist", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            else {
                if (events.getConnections() != null){
                    //connections = connectionsRepository.save(events.getConnections());
                    producer.get().addConnections(events.getConnections());
                }
                else{
                    responseEntity = Util.prepareErrorResponse("404", "Please add connection details", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            Events _event = new Events();
            _event.setEventName(events.getEventName());
            _event.setEventType(events.getEventType());
            _event.setOrderOfEvents(events.getOrderOfEvents());
            _event.setFilters(events.getFilters());
            _event.setConnections(events.getConnections());
            _event.setDataFormat(events.getDataFormat());
            producer.get().addEvents(_event);
            producer = Optional.of(producerRepository.save(producer.get()));
            //Un comment later
            createExchange(producer.get().getProviderName()+"."+events.getEventName());
            responseEntity = Util.prepareResponse(producer.get().getEvents(), HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public void createExchange(String exchangeName){
        String baseUrl = "http://mq-service:9193/api/provider/create/exchange";
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter("exchangeName", exchangeName);

            // Create the URI object from the built URL
            URI uri = uriBuilder.build();
            String str = webClientBuilder.build().get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println(str);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<Object> addEventGroup(long id, EventGroup events){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(!producer.isPresent()){
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                    + " does not exist", HttpStatus.NOT_FOUND);
            return responseEntity;
        }
        if(events.getEventType().equalsIgnoreCase("push")) events.setConnections(null);
        Connections connections = null;
        if(events.getConnections() !=null) {
            if (events.getConnections().getConnectionId() != null){
                if (connectionsRepository.findById(events.getConnections().getConnectionId()).isPresent()) {
                    connections = connectionsRepository.findById(events.getConnections().getConnectionId()).get();
                    events.setConnections(connections);
                } else {
                    responseEntity = Util.prepareErrorResponse("404", "Sorry the connection with Id " + events.getConnections().getConnectionId()
                            + " does not exist", HttpStatus.NOT_FOUND);
                    return responseEntity;
                }
            }
            else{
                String connectionName = events.getConnections().getConnectionName();
                producer.get().addConnections(events.getConnections());
                producerRepository.save(producer.get());
                connections = connectionsRepository.findByConnectionName(id, connectionName).get();
                //connections = connectionsRepository.findById(connectionId).get();
            }
        }
        if(producer.isPresent()) {

            for (String eventname : events.getEventNames()) {
                Events event = new Events();
                event.setEventName(eventname);
                event.setEventType(events.getEventType());
                event.setOrderOfEvents(events.getOrderOfEvents());
                event.setConnections(connections);
                producer.get().addEvents(event);
            }
            producerRepository.save(producer.get());
            responseEntity = Util.prepareResponse(producer.get().getEvents(), HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> updateEvent(long id, Events events) {
        ResponseEntity<Object> responseEntity = null;
        Optional<Events> existingEvent = eventsRepository.findById(id);
        Optional<Producer> producer = eventsRepository.findProviderByEventId(id);
        if (!existingEvent.isPresent()) {
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id " + id
                    + " does not exist", HttpStatus.NOT_FOUND);
            return responseEntity;
        }
        if (events.getEventType().equalsIgnoreCase("pull")) {
            Connections connection = events.getConnections();
            if (connection != null) {
                if (connection.getConnectionId() != null) {
                    Optional<Connections> conn = connectionsRepository.findById(connection.getConnectionId());
                    if (conn.isPresent())
                        connection = conn.get();
                    else {
                        responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with Id " + connection.getConnectionId()
                                + " does not exist", HttpStatus.NOT_FOUND);
                        return responseEntity;
                    }
                } else {
                    if (producer.isPresent()) {
                        Optional<Connections> existingConnection = connectionsRepository.findByConnectionName(producer.get().getProviderId(), events.getConnections().getConnectionName());
                        if (existingConnection.isPresent()) {
                            if (!areConnectionsEqual(events.getConnections(), existingConnection.get())) {
                                producer.get().addConnections(connection);
                                connection = connectionsRepository.save(connection);
                            } else {
                                connection = existingConnection.get();
                            }
                        } else {
                            producer.get().addConnections(connection);
                            connection = connectionsRepository.save(connection);
                        }
                    }
                }
                existingEvent.get().setEventType("pull");
                existingEvent.get().setOrderOfEvents(events.getOrderOfEvents());
                existingEvent.get().setConnections(connection);
                responseEntity = Util.prepareResponse(eventsRepository.save(existingEvent.get()), HttpStatus.OK);
            }
            else {
                responseEntity = Util.prepareErrorResponse("400", "Cannot update type of event to pull as Connection details " +
                        "are missing. Pull type must have at least 1 active connection.", HttpStatus.BAD_REQUEST);
            }

        } else if (events.getEventType().equalsIgnoreCase("push")) {
            existingEvent.get().setEventType(events.getEventType());
            existingEvent.get().setConnections(null);
            existingEvent.get().setOrderOfEvents(events.getOrderOfEvents());
            responseEntity = Util.prepareResponse(existingEvent.get(), HttpStatus.OK);
        } else {
            responseEntity = Util.prepareErrorResponse("400", "Cannot update type of event as it is invalid type", HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    public ResponseEntity<Object> addConnection(long providerId, Connections connection){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(providerId);
        if(producer.isPresent()){
            producer.get().addConnections(connection);
            producerRepository.save(producer.get());
            return responseEntity = Util.prepareResponse(connection,HttpStatus.ACCEPTED);
        }
        return responseEntity = Util.prepareErrorResponse("404", "Sorry Requested Provider does not exist", HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<Object> updateConnection(long connectionId, Connections connection) {
        ResponseEntity<Object> responseEntity;
        Optional<Connections> existingConnection = connectionsRepository.findById(connectionId);
        if(existingConnection.isPresent()) {
            existingConnection.get().setConnectionType(connection.getConnectionType());
            existingConnection.get().setUrl(connection.getUrl());
            existingConnection.get().setUsername(connection.getUsername());
            existingConnection.get().setPassword(connection.getPassword());
            responseEntity = Util.prepareResponse(connectionsRepository.save(existingConnection.get()), HttpStatus.OK);
        }
        else{
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with id "+ connectionId
                    +" does not exist", HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @Transactional
    public String deleteEvent(long id){
        if(eventsRepository.findById(id) != null) {
            subscribedEventsRepository.deleteByEventId(id);
            eventsRepository.deleteById(id);
            return "Event with id "+id+" deleted successfully";
        }
        else throw new ResourceNotFoundException("Not found Event with id = " + id);
    }

    public ResponseEntity<Object> updateProducer(Producer producer){
        ResponseEntity<Object> responseEntity;
        Producer existingProducer = producerRepository.findByProviderName(producer.getProviderName()).orElse(null);
        if(existingProducer == null){
            responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider details" +
                    " does not exist", HttpStatus.NOT_FOUND);
            return responseEntity;
        }
        existingProducer.setProviderTechnicalPOC(producer.getProviderTechnicalPOC());
        existingProducer.setProviderBusinessPOC(producer.getProviderBusinessPOC());
        existingProducer.setAlertNotificationEmailID(producer.getAlertNotificationEmailID());
        producerRepository.save(existingProducer);
        ProviderBasicDetailsDTO providerDTO = providerMapper.toProviderBasicDetailsDTO(existingProducer);
        responseEntity = Util.prepareResponse(providerDTO, HttpStatus.OK);
        return responseEntity;
    }

    public ResponseEntity<Object> activateProvider(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()) {
            if (!producer.get().isActive()) {
                producer.get().setActive(true);
                producerRepository.save(producer.get());
            }
            ProviderBasicDetailsDTO providerDTO = providerMapper.toProviderBasicDetailsDTO(producer.get());
            responseEntity = Util.prepareResponse(providerDTO, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> suspendProvider(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()) {
            if (producer.get().isActive()) {
                producer.get().setActive(false);
                producerRepository.save(producer.get());
            }
            ProviderBasicDetailsDTO providerDTO = providerMapper.toProviderBasicDetailsDTO(producer.get());
            responseEntity = Util.prepareResponse(providerDTO, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> activateEvent(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Events> event = eventsRepository.findById(id);
        if(event.isPresent()) {
            if (!event.get().isActive()) {
                event.get().setActive(true);
                eventsRepository.save(event.get());
            }
            responseEntity = Util.prepareResponse(event, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }
    public ResponseEntity<Object> suspendEvent(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Events> event = eventsRepository.findById(id);
        if(event.isPresent()) {
            if (event.get().isActive()) {
                event.get().setActive(false);
                eventsRepository.save(event.get());
            }
            responseEntity = Util.prepareResponse(event, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested event with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> activateConnection(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Connections> connection = connectionsRepository.findById(id);
        if(connection.isPresent()) {
            if (!connection.get().isActive()) {
                connection.get().setActive(true);
                connectionsRepository.save(connection.get());
            }
            responseEntity = Util.prepareResponse(connection, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> suspendConnection(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Connections> connection = connectionsRepository.findById(id);
        if(connection.isPresent()) {
            if (connection.get().isActive()) {
                connection.get().setActive(false);
                connectionsRepository.save(connection.get());
            }
            responseEntity = Util.prepareResponse(connection, HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested connection with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> deleteProducer(long id){
        ResponseEntity<Object> responseEntity;
        Optional<Producer> producer = producerRepository.findById(id);
        if(producer.isPresent()){
            //kafkaTopicConfig.deleteTopic(producer.get().getKafkaTopicName());
            producerRepository.deleteById(id);
            responseEntity = Util.prepareResponse("Producer with id " + id + " deleted successfully.", HttpStatus.OK);
        }
        else responseEntity = Util.prepareErrorResponse("404", "Sorry the requested provider with Id " + id
                + " does not exist", HttpStatus.NOT_FOUND);
        return responseEntity;
    }

    public ResponseEntity<Object> getFiltersByEventId(long eventId) {
//        List<String> filters = new ArrayList<String>();
//        filters.add("location.longitude");
//        Stream<String> stream = filters.stream().sorted();
//        filters = stream.collect(Collectors.toList());
        Optional<Events> events = eventsRepository.findById(eventId);
        if(events.isPresent()){
            return Util.prepareResponse(events.get().getFilters(), HttpStatus.OK);
        }
        return Util.prepareErrorResponse("404", "Event with Id "+ eventId + " not found", HttpStatus.NOT_FOUND);

    }


//return new ResponseEntity<>(connectionParameters, HttpStatus.CREATED);

}
