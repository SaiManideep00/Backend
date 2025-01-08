package com.mm.engine.config;


import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
@Order(2)
public class LoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {

            MDC.put("method", httpRequest.getMethod());
            MDC.put("path", httpRequest.getRequestURI());
            AWSXRay.getCurrentSegmentOptional().ifPresent(segment -> {
                String traceId = segment.getTraceId().toString();
                MDC.put("traceId", traceId);
            });

            chain.doFilter(request, response);
        }

        finally {
            MDC.clear(); // Clean up after the request is processed

        }}
}

