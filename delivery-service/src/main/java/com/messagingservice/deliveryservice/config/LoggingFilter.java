package com.messagingservice.deliveryservice.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
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
public class LoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // Cast to HttpServletRequest to access HTTP-specific methods
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Log basic request info and add context to logs
//            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("method", httpRequest.getMethod());
            MDC.put("path", httpRequest.getRequestURI());




            // Pass the request along the filter chain
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // Clean up after the request is processed
        }
    }
}
