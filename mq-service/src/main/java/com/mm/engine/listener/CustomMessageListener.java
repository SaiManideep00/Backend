package com.mm.engine.listener;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Slf4j
public class CustomMessageListener implements ChannelAwareMessageListener {

    //amqpAdmin.getBindings().get(queueName);

    private final RabbitAdmin rabbitAdmin;
    private final RabbitListenerEndpointRegistry endpointRegistry;
    private final WebClient.Builder webClientBuilder;

    public CustomMessageListener(RabbitAdmin rabbitAdmin, RabbitListenerEndpointRegistry endpointRegistry, WebClient.Builder webClientBuilder) {
        this.rabbitAdmin = rabbitAdmin;
        this.endpointRegistry = endpointRegistry;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        MessageProperties messageProperties = message.getMessageProperties();
        String consumerTag = messageProperties.getConsumerTag();
        String queueName = messageProperties.getConsumerQueue();
        String providerName = messageProperties.getReceivedExchange().split("\\.",-2)[0];
        String subscriptionType = queueName.split("\\.",-2)[1];
        if(messageProperties.getHeader("retry_count") == null)
            message.getMessageProperties().setHeader("retry_count",1);
        else{
            int c = message.getMessageProperties().getHeader("retry_count");
            message.getMessageProperties().setHeader("retry_count",c+1);
            log.info(message.getMessageProperties().getHeader("retry_count").toString());
        }

        String content = new String(message.getBody());
        log.info("Event Name: " + messageProperties.getReceivedExchange());
        log.info("Queue Name: " + queueName);
        log.info("Subscription Type: " + subscriptionType);
        log.info("Actual Content: " + content);

        processMessage(content, queueName, providerName);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
//        }
//        catch (Exception e){
////            Thread.sleep(50000);
//            //channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
//            //log.info("message retry count: " + message.getMessageProperties().getHeader("retry_count"));
//            //log.info(endpointRegistry.getListenerContainer(queueName).getMessageListener().toString());
//            //endpointRegistry.getListenerContainer(queueName).stop();
//            if(message.getMessageProperties().getHeader("retry_count").toString() == "5")
//                endpointRegistry.getListenerContainer(queueName).stop();
//            log.error("in Catch block: {}",e.getMessage());
//        }
        // Process the message
    }

    public void processMessage(String content, String queueName, String providerName) throws Exception {
        String baseUrl = "http://delivery-service:9192/api/deliver";
        //WebClient webClient = WebClient.builder().build();
        ResponseEntity<String> response;
        try {
            // Build the URL with query parameters
            URIBuilder uriBuilder = new URIBuilder(baseUrl);
            URI uri = uriBuilder.build();
//            JsonObject requestBody = new JsonObject();
//            requestBody.addProperty("queueName", "queueName");
//            Gson gson = new Gson();
//            String jsonBody = gson.toJson(content);
//            System.out.println("Json Body: "+ jsonBody);
            response = webClientBuilder.build().post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("providerName", providerName)
                    .header("eventName", queueName.split("\\.",-2)[2])
                    .header("consumerName", queueName.split("\\.",-2)[0])
                    .header("subscriptionType", queueName.split("\\.",-2)[1])
                    .body(BodyInserters.fromValue(content))
                    .retrieve().toEntity(String.class)
                    .block();
            log.info("Delivery Successful : {}", response.getBody());

//            System.out.println(response.getBody());
            //connectionFactory.createConnection().createChannel(true).queueUnbind(queueName, exchangeName, "routetoall");

        }
        catch (Exception listenerExecutionFailedException){
            System.out.println("Exception Occurred");
            System.out.println(listenerExecutionFailedException.getMessage());
//            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
//            SimpleMessageListenerContainer listenerContainer = (SimpleMessageListenerContainer) rabbitListenerEndpointRegistry.getListenerContainer(queueName);
            // Suspend the listener
//            log.info("Container Map: {}", containerMap.get(queueName));
//            containerMap.get(queueName).stop();
            throw new Exception(listenerExecutionFailedException.getMessage());
        }
        if(!(response.getStatusCode().equals(HttpStatus.ACCEPTED) || response.getStatusCode().equals(HttpStatus.OK))){
            log.info("Delivery Failed");
        }
        log.info(response.getStatusCode().toString());
        log.info(response.getBody().toString());
    }

}
