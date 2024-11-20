package com.mm.engine.controller;

import com.mm.engine.dto.ConsumerRequest;
import com.mm.engine.listener.CustomMessageListener;
import com.mm.engine.listenerservice.ListenerService;
import com.mm.engine.recoverer.CustomMessageRecoverer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

@RestController
@Slf4j
public class ListenerController {

    private final ConnectionFactory connectionFactory;
    private final RabbitAdmin rabbitAdmin;
    private final SimpleMessageListenerContainer container;
    private final SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory;
    private final RabbitListenerEndpointRegistry endpointRegistry;
    private final WebClient.Builder webClientBuilder;

    private final ListenerService listenerService;

    private DynamicContainerLister dynamicContainerLister;
    //@Qualifier("retryContainerFactory")
    public ListenerController(ConnectionFactory connectionFactory,
                              RabbitAdmin rabbitAdmin, SimpleMessageListenerContainer container, SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory, RabbitListenerEndpointRegistry endpointRegistry, WebClient.Builder webClientBuilder, ListenerService listenerService, DynamicContainerLister dynamicContainerLister) {
        this.connectionFactory = connectionFactory;
        this.rabbitAdmin = rabbitAdmin;
        this.container = container;
        this.simpleRabbitListenerContainerFactory = simpleRabbitListenerContainerFactory;
        this.endpointRegistry = endpointRegistry;
        this.webClientBuilder = webClientBuilder;
        this.listenerService = listenerService;
        this.dynamicContainerLister = dynamicContainerLister;
    }

    @PostMapping("/create/listener")
    public String createConsumer(@RequestBody ConsumerRequest request) {
        String queueName = request.getQueueName();
        // Declare the queue, exchange, and binding
        rabbitAdmin.declareQueue(new Queue(queueName, true, false, false));
        createListener(queueName);
        // Create and configure the consumer
        /*
        SimpleMessageListenerContainer smlc = simpleRabbitListenerContainerFactory.createListenerContainer();
        smlc.setConnectionFactory(connectionFactory);
        smlc.setQueueNames(queueName);
//        smlc.setAdviceChain(RetryInterceptorBuilder.stateless()
//                .maxAttempts(5)
//                .backOffOptions(10000, 2.0, 100000)
//                .recoverer()
//                .build());
        smlc.start();
//        //MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(new CustomMessageListener(rabbitAdmin));
//        container.addQueueNames(queueName);
//        container.setMessageListener(listenerAdapter);
//        container.setAdviceChain(RetryInterceptorBuilder.stateless()
//                .maxAttempts(5)
//                .backOffOptions(1_000, 2.0, 10_000)
//                .build());
////        container.setRetryDeclarationInterval(10000);
//        container.start();
        log.info("No. of active consumers: {}",smlc.getActiveConsumerCount());
*/
        return "Consumer created successfully.";
    }

    private void createListener(String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        //container.setConcurrentConsumers(1);
        container.setPrefetchCount(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setMessageListener(new MessageListenerAdapter(new CustomMessageListener(rabbitAdmin, endpointRegistry, webClientBuilder)));
        container.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(10000, 1, 100000)
                .recoverer(new CustomMessageRecoverer(endpointRegistry, dynamicContainerLister, listenerService))
                .build());
        container.setListenerId(queueName);
        container.start();
        dynamicContainerLister.registerDynamicContainer(queueName, container);
        //endpointRegistry.registerListenerContainer();
        log.info("Queue Name: {}",queueName);
        log.info("Listener ID: {}",container.getListenerId());
        log.info("Listener started: {}",dynamicContainerLister.getDynamicContainer(queueName));
        log.info("No. of Listeners: {}",dynamicContainerLister.getSize());
//        Collection<MessageListenerContainer> list = endpointRegistry.getListenerE
//        for (MessageListenerContainer mlc : list) {
//            log.info("Listner: {}",mlc.toString());
//        }
//        endpointRegistry.start();
//        if(endpointRegistry.){
//            log.info("Running...");
//        }
//        dynamicContainerLister.getDynamicContainer(queueName).stop();
//        log.info("Stopped listener, status: {}",dynamicContainerLister.getDynamicContainer(queueName).isRunning());
//        return "Listener created for queue: " + queueName;
    }


    @PostMapping("/bind")
    public String bindQueueToExchange(@RequestBody ConsumerRequest request) {
        String queueName = request.getQueueName();
        String exchangeName = request.getExchangeName();
        rabbitAdmin.declareQueue(new Queue(queueName, true, false, false));
        //rabbitAdmin.removeBinding();
        createListener(queueName);
        rabbitAdmin.declareBinding(new Binding(queueName, Binding.DestinationType.QUEUE, exchangeName,"", null));
        return "exchange: " + exchangeName + "bound to queue: " + queueName + " successfully.";
    }

    @PutMapping("/unbind")
    public String unbindQueueFromExchange(@RequestBody ConsumerRequest request) {
//        rabbitAdmin.removeBinding(rabbitAdmin.getQueueProperties(queueName).getProperty());
        String queueName = request.getQueueName();
        String exchangeName = request.getExchangeName();
        try {
            connectionFactory.createConnection().createChannel(true).queueUnbind(queueName, exchangeName, "routetoall");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(rabbitAdmin.getQueueProperties(queueName));
        return "Queue: " + queueName + " unbound from exchange: " + exchangeName + "successfully.";
    }
    @PutMapping("/start/listener")
    public ResponseEntity<String> startListener(@RequestBody ConsumerRequest request){
        String queueName = request.getQueueName();
        try {
           // endpointRegistry.getListenerContainer(queueName).start();
            dynamicContainerLister.getDynamicContainer(queueName).start();
            if(dynamicContainerLister.getDynamicContainer(queueName).isActive())
                log.info("Listener status: "+"Listener Started..");
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Consumer Started");
    }

    @PutMapping("/stop/listener")
    public ResponseEntity<String> stopListener(@RequestBody ConsumerRequest request){
        String queueName = request.getQueueName();
        try {
            // endpointRegistry.getListenerContainer(queueName).start();
            dynamicContainerLister.getDynamicContainer(queueName).stop();
            if(!dynamicContainerLister.getDynamicContainer(queueName).isActive())
                log.info("Listener status: "+"Listener Stopped..");
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
        return ResponseEntity.ok().body("Consumer Stopped");
    }

}

