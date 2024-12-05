//package com.messagingservice.backendservice.config;
//
//import io.micrometer.observation.ObservationRegistry;
//import io.micrometer.observation.aop.ObservedAspect;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class ObservationConfig {
//    @Bean
//    public ObservedAspect observedAspect(ObservationRegistry observationRegistry){
//        observationRegistry.observationConfig().observationHandler(new PerformanceTrackerHandler());
//        return new ObservedAspect(observationRegistry);
//    }
//}
