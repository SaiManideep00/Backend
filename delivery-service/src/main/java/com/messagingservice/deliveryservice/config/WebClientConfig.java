package com.messagingservice.deliveryservice.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.servlet.Filter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.codec.http.HttpHeaders;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(addXRayTraceHeader()); // Add the custom filter for X-Ray trace propagation
    }

    private ExchangeFilterFunction addXRayTraceHeader() {
        return (request, next) -> {
            ClientRequest.Builder requestBuilder = ClientRequest.from(request);

            // Get the current segment and add the trace header if present
            AWSXRay.getCurrentSegmentOptional().ifPresent(segment -> {
                String traceHeader = TraceHeader.fromEntity(segment).toString();
                requestBuilder.header("X-Amzn-Trace-Id", traceHeader);
                System.out.println("TraceHeader added to request: " + traceHeader);
            });

            // Proceed with the modified request
            return next.exchange(requestBuilder.build());
        };
    }

}
