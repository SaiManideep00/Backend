package com.mm.engine.config;

import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.servlet.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
@Order(1)
public class XrayConfig {
    @Bean
    public Filter TracingFilter()
    {
        return new AWSXRayServletFilter("MQ-Service");
    }
}
