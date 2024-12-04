package com.mm.engine.listenerservice;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
@Slf4j
@XRayEnabled
public class ListenerService {
    private final WebClient.Builder webClientBuilder;
    public void suspendConsumer(String queueName, String exchangeName) throws Exception {
        String baseUrl = "http://BackendServiceALB-788612567.us-east-2.elb.amazonaws.com/api/suspend/subscription";
        ResponseEntity<Object> response;
        try {
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("eventName", queueName.split("\\.",-2)[2]);
            requestBody.addProperty("consumerName", queueName.split("\\.",-2)[0]);
            requestBody.addProperty("providerName", exchangeName.split("\\.",-2)[0]);
            requestBody.addProperty("subscriptionType", queueName.split("\\.",-2)[1]);
            Gson gson = new Gson();
            String jsonBody = gson.toJson(requestBody);
            response = webClientBuilder.build().put()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(BodyInserters.fromValue(jsonBody))
                    .retrieve().toEntity(Object.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(response.getStatusCode().equals(HttpStatus.ACCEPTED) || response.getStatusCode().equals(HttpStatus.OK)){
            log.info("Subscription Suspended Successfully");
        }
        else{
            log.warn("Subscription Suspend Failed",response.getBody());

        }
        System.out.println(response.getStatusCode());
        System.out.println(response.getBody());
    }
}
