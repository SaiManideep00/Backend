package com.messagingservice.deliveryservice.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.servlet.Filter;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(addXRayTraceHeader()); // Add X-Ray trace propagation filter
    }

    private ExchangeFilterFunction addXRayTraceHeader() {
        return (request, next) -> {
            // Get the current trace header
            String traceHeader = AWSXRay.getCurrentSegmentOptional()
                    .map(segment -> segment.getTraceId().toString())
                    .orElse(null);

            if (traceHeader != null) {
                // Add the trace header to the outgoing request
                ClientRequest modifiedRequest = ClientRequest.from(request)
                        .header("X-Amzn-Trace-Id", traceHeader)
                        .build();
                return next.exchange(modifiedRequest);
            }

            return next.exchange(request);
        };
    }



}
