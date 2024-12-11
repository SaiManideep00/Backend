package com.mm.engine.recoverer;

import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.mm.engine.controller.DynamicContainerLister;
import com.mm.engine.listenerservice.ListenerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;

@Slf4j
@XRayEnabled
public class CustomMessageRecoverer implements MessageRecoverer {
    private final RabbitListenerEndpointRegistry endpointRegistry;
    private DynamicContainerLister dynamicContainerLister;
    private ListenerService listenerService;
    public CustomMessageRecoverer(RabbitListenerEndpointRegistry endpointRegistry, DynamicContainerLister dynamicContainerLister, ListenerService listenerService) {
        this.endpointRegistry = endpointRegistry;
        this.dynamicContainerLister = dynamicContainerLister;
        this.listenerService = listenerService;
    }
    @Override
    public void recover(Message message, Throwable cause) {
        int retry_count = message.getMessageProperties().getHeader("retry_count");
        log.info("Retried {} times", retry_count);
        String queueName = message.getMessageProperties().getConsumerQueue();
        log.info("QueueName: {}",queueName);
        log.info("No. of Listeners: {}",endpointRegistry.getListenerContainerIds().size());
        String listenerId = message.getMessageProperties().getConsumerTag();
        if (listenerId != null) {
            dynamicContainerLister.getDynamicContainer(queueName).stop();
            // Perform any other custom recovery actions for this specific listener
            try {
                listenerService.suspendConsumer(queueName,message.getMessageProperties().getReceivedExchange());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
