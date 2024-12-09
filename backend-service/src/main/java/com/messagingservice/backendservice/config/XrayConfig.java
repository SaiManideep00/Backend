package com.messagingservice.backendservice.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XrayConfig {
    @Bean
    public Filter TracingFilter()
    {
        return new AWSXRayServletFilter("Backend-Service");
    }
    @Bean
    public AWSXRayRecorder xrayRecorder() {
        return AWSXRay.getGlobalRecorder();
    }
}
