package com.messagingservice.deliveryservice.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.listeners.SegmentListener;
import com.amazonaws.xray.slf4j.SLF4JSegmentListener;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration

public class XrayConfig {
    @Bean
    public Filter TracingFilter()
    {
        return new AWSXRayServletFilter("Delivery-Service");
    }
    @PostConstruct
    public void init() {

        AWSXRay.getGlobalRecorder().addSegmentListener(new SLF4JSegmentListener("AWS-XRAY-TRACE-ID"));
    }
}
