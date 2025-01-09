package com.mm.engine.config;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.jakarta.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.slf4j.SLF4JSegmentListener;
import com.amazonaws.xray.strategy.jakarta.SegmentNamingStrategy;
import jakarta.annotation.PostConstruct;
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
    @PostConstruct
    public void init() {

        AWSXRay.getGlobalRecorder().addSegmentListener(new SLF4JSegmentListener("AWS-XRAY-TRACE-ID"));
    }
}
