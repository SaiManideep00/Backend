package com.messagingservice.deliveryservice.service;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.messagingservice.deliveryservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.mail.MessagingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@XRayEnabled
public class DeliveryService {
    private final WebClient.Builder webClientBuilder;
    private final Gson gson;
    private final JSONParserService jsonParserService;
    private final XMLParserService xmlParserService;
    private final XmlMapper xmlMapper = new XmlMapper();
    public ResponseEntity<String> deliverViaRestAPI(Object object, SubscribedEventConnections subscribedEventConnections){
        String baseUrl = subscribedEventConnections.getUrl();
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
//            JsonObject requestBody = new JsonObject();
//            requestBody.addProperty("queueName", "queueName");
            String jsonBody = gson.toJson(object);
            ResponseEntity<Void> response = webClientBuilder.build().post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve().toBodilessEntity()
                    .block();
            System.out.println(response.getStatusCode());
            if(response.getStatusCode().equals(HttpStatus.ACCEPTED) || response.getStatusCode().equals(HttpStatus.OK)){
                log.info("Delivered succesfully via rest API");
                return ResponseEntity.ok().body("Delivered Successfully");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        log.error("delivery failed");
        return ResponseEntity.ok().body("Delivery Failed");
    }

    public ResponseEntity<String> writeToDB(Object obj, SubscribedEventConnections subscribedEventConnections){
        String url = subscribedEventConnections.getUrl();
        String username = subscribedEventConnections.getUsername();
        String password = subscribedEventConnections.getPassword();
        System.out.println(obj.toString());
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            String sql = "INSERT INTO demodata VALUES (?,?)";
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            // Set the value for Lob
            statement.setInt(1, 1); // Replace yourLobData with your actual LOB data
            statement.setString(2, (String) obj.toString()); // Replace yourLobData with your actual LOB data

            int rowsInserted = statement.executeUpdate();
            // Close the statement and connection
            statement.close();
            connection.close();
            if (rowsInserted > 0) {
                log.info("Successfully written to DB");
                return ResponseEntity.ok().body("Delivered Successfully");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            ResponseEntity.badRequest().body(e.getMessage());
            //ResponseEntity.internalServerError().body(e.getMessage());
        }
        log.error("Delivery failed");
        return ResponseEntity.badRequest().body("Delivery Failed");
    }

    public boolean validateFilters(Map<String, List<List<String>>> message, List<Filter> filters){
        log.info("Validating filters");
        if(filters.size() == 0)
            return true;
        for(Filter filter : filters){
            if(! message.containsKey(filter.getFilterKey())) {
                log.error("Cannot find filter");
                return false;
            }
            List<List<String>> lists = message.get(filter.getFilterKey());
            boolean flag = false;
            for(List<String> values : lists){
                for(String value : values){
                    if(value.equalsIgnoreCase(filter.getFilterValue()))
                        flag = true;
                }
            }
            if(!flag)
                return false;
        }
        return true;
    }
    public ResponseEntity<String> identifyDeliveryMethodAndDeliver(String providerName, String eventName, String consumerName, Object obj) {
        String baseUrl = "http://backend-service:9191/api/get/subscribed_event";
        ResponseEntity<SubscribedEvent> response;
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            response = webClientBuilder.build().get()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("providerName", providerName)
                    .header("eventName", eventName)
                    .header("consumerName", consumerName)
                    .retrieve().toEntity(SubscribedEvent.class)
                    .block();
            log.info("fetched subscribed event "+response);
            System.out.println(response);
        } catch (URISyntaxException e) {
            log.info("Cannot fetch event");
            throw new RuntimeException(e);
        }
        SubscribedEvent subscribedEvent = response.getBody();
        Map<String, List<List<String>>> message;
        if(subscribedEvent.getSourceDataFormat().equalsIgnoreCase("json")){
            String jsonBody = gson.toJson(obj);
            log.info("JSON is "+jsonBody);
            System.out.println(jsonBody);
            message = jsonParserService.jsonToMap(jsonBody);
        }
        else{
            try{
                String xmlBody = xmlMapper.writeValueAsString(obj);
                log.info("XML is "+xmlBody);
                System.out.println(xmlBody);
                message = xmlParserService.xmlToMap(xmlBody);
            } catch (JsonProcessingException e) {
                log.error("JSon Processing Exception");
                throw new RuntimeException(e);
            } catch (JSONException e) {
                log.error("Json Exception");
                throw new RuntimeException(e);
            }

        }
        List<Filter> filters = subscribedEvent.getFilters();
        if(!validateFilters(message,filters)){
            log.error("Filters did not match, message discarded");
            return ResponseEntity.ok().body("Filters did not match, message discarded");
        }
        if (subscribedEvent.getSubscribedEventConnections().getConnectionType().equals("https") || subscribedEvent.getSubscribedEventConnections().getConnectionType().equals("http")) {
            return deliverViaRestAPI(obj, subscribedEvent.getSubscribedEventConnections());
        }
        return writeToDB(obj, subscribedEvent.getSubscribedEventConnections());
    }

    public ResponseEntity<String> notify(String providerName, String eventName, String consumerName, Object obj) {
        String baseUrl = "http://backend-service:9191/api/get/subscribed_alert";
        ResponseEntity<AlertSubscription> response;
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            response = webClientBuilder.build().get()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("providerName", providerName)
                    .header("eventName", eventName)
                    .header("consumerName", consumerName)
                    .retrieve().toEntity(AlertSubscription.class)
                    .block();
        } catch (URISyntaxException e) {
            log.error("Cannot find URL");
            throw new RuntimeException(e);
        }
        AlertSubscription alertSubscription = response.getBody();
        String jsonBody = gson.toJson(obj);
        //System.out.println(jsonBody);
        Map<String, List<List<String>>> message = JSONParserService.jsonToMap(jsonBody);
        Set<DBWatchlist> dbWatchlists = alertSubscription.getWatchlists();
        List<String> matchCriteria = alertSubscription.getAlertCriteria().getMatchCriteria();
        String matchedCriteria = checkMatchCriteria(dbWatchlists,matchCriteria,message);
        if(!matchedCriteria.isEmpty()){
            String subject = eventName + " Alert: Criteria Matched";
            String[] values = {eventName,providerName,matchedCriteria};
            try {

                EmailSender.sendHtmlEmail(alertSubscription.getAlertCriteria().getRecepientGroup().getEmailIds(), subject, values);
                log.info("Sent email ");
            } catch (MessagingException e) {
                log.error("Message Exception");
                throw new RuntimeException(e);
            }
            log.info("Notified Recipients...");
            log.info("Criteria Matched for the alert subscription and Notified Successfully");
            return ResponseEntity.ok().body("Criteria Matched for the alert subscription and Notified Successfully");
        }
        log.error("Criteria did not match, message discarded");
        return ResponseEntity.ok().body("Criteria did not match, message discarded");
    }

    public String checkMatchCriteria(Set<DBWatchlist> dbWatchlists, List<String> matchCriteria, Map<String, List<List<String>>> message){
        for(DBWatchlist dbWatchlist : dbWatchlists){
            for(String key : matchCriteria){
                //String value = "";
//                if(message.containsKey(key)){
//                    value = message.get(key).toString();
//                }
                List<List<String>> lists = message.get(key);
                boolean flag = false;
                for(List<String> values : lists){
                    for(String value : values){
                        if(DatabaseHelper.searchInDatabase(key.replace('.','_'), value, dbWatchlist.getUrl(),dbWatchlist.getUsername(),dbWatchlist.getPassword())){
                            return key;
                        }
                    }
                }
            }
        }
        return "";
    }

    public ResponseEntity<String> publishDataToMQ(String providerName, String eventName, Object data){
        String baseUrl = "http://mq-service:9193/api/v1/publish";
        ResponseEntity<String> response;
        String jsonBody = gson.toJson(data);
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            uriBuilder.addParameter("exchange", providerName+"."+eventName);
            uriBuilder.addParameter("routingKey", "");
            URI uri = uriBuilder.build();
            response = webClientBuilder.build().post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(data))
                    .retrieve().toEntity(String.class)
                    .block();
            log.info("published data to MQ "+response.getBody());
            Segment segment =AWSXRay.getCurrentSegmentOptional().orElse(null);
            String traceId=null;
            if(segment!=null)
                 traceId=segment.getTraceId().toString();
            System.out.println("Trace ID sent is "+traceId);
            System.out.println(response);
        } catch (URISyntaxException e) {
            log.error("Invalid URI");
            throw new RuntimeException(e);
        }
        return response;
    }


}
