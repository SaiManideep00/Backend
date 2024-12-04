package com.mm.engine.controller;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@XRayEnabled
@Slf4j
public class DynamicContainerLister {

    private Map<String, SimpleMessageListenerContainer> dynamicContainers = new HashMap<>();

    public SimpleMessageListenerContainer getDynamicContainer(String queueName) {
        return dynamicContainers.get(queueName);
    }

    public void registerDynamicContainer(String queueName, SimpleMessageListenerContainer container) {
        log.info("Listener Added "+queueName+"  "+container.toString());
        System.out.println("Listener Added");
        dynamicContainers.put(queueName,container);
    }

    public int getSize(){
        log.info("size "+ dynamicContainers.size());
        System.out.println("size "+ dynamicContainers.size());
        return dynamicContainers.size();
    }

    public void unregisterDynamicContainer(String queueName) {
        log.info("Removed queue "+queueName);
        dynamicContainers.remove(queueName);
    }
}

