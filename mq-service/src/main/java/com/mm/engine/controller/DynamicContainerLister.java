package com.mm.engine.controller;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicContainerLister {

    private Map<String, SimpleMessageListenerContainer> dynamicContainers = new HashMap<>();

    public SimpleMessageListenerContainer getDynamicContainer(String queueName) {
        return dynamicContainers.get(queueName);
    }

    public void registerDynamicContainer(String queueName, SimpleMessageListenerContainer container) {
        System.out.println("Listener Added");
        dynamicContainers.put(queueName,container);
    }

    public int getSize(){
        System.out.println("size "+ dynamicContainers.size());
        return dynamicContainers.size();
    }

    public void unregisterDynamicContainer(String queueName) {
        dynamicContainers.remove(queueName);
    }
}

