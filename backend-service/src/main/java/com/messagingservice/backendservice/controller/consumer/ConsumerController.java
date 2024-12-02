package com.messagingservice.backendservice.controller.consumer;


import com.amazonaws.xray.spring.aop.XRayEnabled;
import com.messagingservice.backendservice.dto.consumer.AlertSubscriptionDTO;
import com.messagingservice.backendservice.dto.consumer.SubscriptionFinderDTO;
import com.messagingservice.backendservice.dto.consumer.SubscribedEventsGroup;
import com.messagingservice.backendservice.model.consumer.*;
import com.messagingservice.backendservice.services.consumer.ConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/")
//@CrossOrigin(origins = "http://prodeucerconsumerfrontend.s3-website-us-east-1.amazonaws.com/")
@CrossOrigin("*")
@XRayEnabled
public class ConsumerController {

    private final ConsumerService consumerService;

    public ConsumerController(ConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @PostMapping("add/consumer")
    public ResponseEntity<Object> addConsumer(@RequestBody Consumer consumer){
        return consumerService.saveConsumer(consumer);
    }

    @GetMapping("get/consumers")
    public ResponseEntity<Object> getConsumers(){
        log.info("[Consumer Controller : ] ");
        return consumerService.getConsumers();
    }

    @GetMapping("get/consumer/{id}")
    public ResponseEntity<Object> getConsumerById(@PathVariable Long id){
        return consumerService.getConsumerById(id);
    }

    @GetMapping("get/consumer/events/{consumerId}")
    public ResponseEntity<Object> getConsumerEventsByConsumerId(@PathVariable Long consumerId){
        return consumerService.getConsumerEventsByConsumerId(consumerId);
    }

    @GetMapping("get/consumer/alert_subscriptions/{consumerId}")
    public ResponseEntity<Object> getConsumerAlertsByConsumerId(@PathVariable Long consumerId){
        return consumerService.getConsumerAlertsByConsumerId(consumerId);
    }

    @GetMapping("get/consumer/subscribed_event/{subscribedEventId}")
    public ResponseEntity<Object> getSubscribedEventById(@PathVariable Long subscribedEventId){
        return consumerService.getSubscribedEventById(subscribedEventId);
    }

    @GetMapping("get/consumer/connections/{consumerId}")
    public ResponseEntity<Object> getConsumerConnectionsById(@PathVariable Long consumerId){
        return consumerService.getConsumerConnectionsById(consumerId);
    }

    @PutMapping("update/consumer")
    public ResponseEntity<Object> updateConsumer(@RequestBody Consumer consumer){
        return consumerService.updateConsumer(consumer);
    }

    @PutMapping("subscribe/event/consumer/{consumerId}")
    public ResponseEntity<Object> subscribeEvent(@PathVariable long consumerId, @RequestBody SubscribedEvents subscribedEvents){

        return consumerService.subscribeEventsByConsumerId(consumerId, subscribedEvents);
    }
    @PutMapping("subscribe/alert/consumer/{consumerId}")
    public ResponseEntity<Object> subscribeAlert(@PathVariable long consumerId, @RequestBody AlertSubscriptionDTO alertSubscriptionDTO){
        return consumerService.subscribeAlertsByConsumerId(consumerId, alertSubscriptionDTO);
    }

    @PutMapping("subscribe/event_group/consumer/{consumerId}")
    public ResponseEntity<Object> subscribeEventGroup(@PathVariable long consumerId, @RequestBody SubscribedEventsGroup subscribedEventsGroup){
        return consumerService.subscribeEventsGroupByConsumerId(consumerId, subscribedEventsGroup);
    }

    @PutMapping("unsubscribe/subscription/{subscriptionId}")
    public ResponseEntity<Object> unsubscribeEventGroup(@PathVariable long subscriptionId){
        return consumerService.unsubscribeEventsBySubscriptionId(subscriptionId);
    }

    @PutMapping("update/consumer/event/{eventId}")
    public ResponseEntity<Object> updateEvent(@PathVariable long eventId, @RequestBody SubscribedEvents subscribedEvents){
        return consumerService.updateSubscribedEvent(eventId, subscribedEvents);
    }

    @PutMapping("update/consumer/alert/{alertId}")
    public ResponseEntity<Object> updateAlert(@PathVariable long alertId, @RequestBody AlertSubscriptionDTO alertSubscription){
        return consumerService.updateAlertSubscription(alertId, alertSubscription);
    }

    @PutMapping("update/consumer/connection/{connectionId}")
    public ResponseEntity<Object> updateConnection(@PathVariable long connectionId, @RequestBody SubscribedEventConnections connection){
        return consumerService.updateConnection(connectionId, connection);
    }

    @PutMapping("activate/consumer/{consumerId}")
    public ResponseEntity<Object> activate(@PathVariable long consumerId){
        return consumerService.activateConsumer(consumerId);
    }

    @PutMapping("suspend/consumer/{consumerId}")
    public ResponseEntity<Object> suspend(@PathVariable long consumerId){
        return consumerService.suspendConsumer(consumerId);
    }

    @PutMapping("activate/subscribedEvent/{eventId}")
    public ResponseEntity<Object> activateSubscribedEvent(@PathVariable long eventId){
        return consumerService.activateSubscribedEvent(eventId);
    }

    @PutMapping("suspend/subscribedEvent/{eventId}")
    public ResponseEntity<Object> suspendSubscribedEvent(@PathVariable long eventId){
        return consumerService.suspendSubscribedEvent(eventId);
    }

    @PutMapping("suspend/subscription")
    public ResponseEntity<Object> suspendSubscription(@RequestBody SubscriptionFinderDTO subscriptionFinderDTO){
        if(subscriptionFinderDTO.getSubscriptionType().equalsIgnoreCase("event"))
            return consumerService.suspendEventSubscription(subscriptionFinderDTO.getEventName(), subscriptionFinderDTO.getConsumerName(), subscriptionFinderDTO.getProviderName());
        return consumerService.suspendAlertSubscription(subscriptionFinderDTO.getEventName(), subscriptionFinderDTO.getConsumerName(), subscriptionFinderDTO.getProviderName());
    }
    @PutMapping("activate/consumer/connection/{connectionId}")
    public ResponseEntity<Object> activateConnection(@PathVariable int connectionId){
        return consumerService.activateConnection(connectionId);
    }
    @PutMapping("suspend/consumer/connection/{connectionId}")
    public ResponseEntity<Object> suspendConnection(@PathVariable long connectionId){
        return consumerService.suspendConnection(connectionId);
    }
    @DeleteMapping("delete/consumer/event/{eventId}")
    public String deleteEvent(@PathVariable long eventId){
        return consumerService.deleteConsumerEvent(eventId);
    }

    @DeleteMapping("delete/consumer/{id}")
    public String deleteConsumer(@PathVariable Integer id){
        return consumerService.deleteConsumer(id);
    }
    @GetMapping("get/connection/subscribed_event")
    public ResponseEntity<Object> getConnectionOfSubscribedEvent(@RequestHeader String consumerName,
                                                                 @RequestHeader String providerName,
                                                                 @RequestHeader String eventName) {
        return consumerService.getConnectionOfSubscribedEvent(providerName, eventName, consumerName);
    }
    @GetMapping("get/subscribed_event")
    public ResponseEntity<Object> getSubscriptionByEventNameAndProviderName(@RequestHeader String providerName,
                                                                            @RequestHeader String consumerName,
                                                                            @RequestHeader String eventName){
        return consumerService.getSubscribedEvent(providerName, eventName, consumerName);
    }
    @GetMapping("get/subscribed_alert")
    public ResponseEntity<Object> getAlertByEventNameAndProviderName(@RequestHeader String providerName,
                                                                            @RequestHeader String consumerName,
                                                                            @RequestHeader String eventName){
        return consumerService.getSubscribedAlert(providerName, eventName, consumerName);
    }
    @PostMapping("add/db_connection")
    public ResponseEntity<Object> addDBConnectionToWatchlist(@RequestBody DBWatchlist dbWatchlist){
        return consumerService.saveDBConnection(dbWatchlist);
    }
    @PutMapping("/subscribed_alerts/{alertId}/db_connection")
    public ResponseEntity<Object> addDBConnectionToAlertSubscription(@PathVariable Long alertId, @RequestBody DBWatchlist dbWatchlist){
        return consumerService.addDBConnectionToAlertSubscription(alertId,dbWatchlist);
    }

    @GetMapping("/get/db_watchlist")
    public ResponseEntity<Object> getDBWatchList(){
        return consumerService.getDBWatchlist();
    }

    @PostMapping("add/recipient_group")
    public ResponseEntity<Object> addRecipientGroup(@RequestBody RecepientGroup recepientGroup){
        return consumerService.saveRecipientGroup(recepientGroup);
    }

    @GetMapping("/get/recipientList")
    public ResponseEntity<Object> getRecipientList(){
        return consumerService.getRecipientList();
    }
    @PutMapping("suspend/alert_subscription/{alertId}")
    public ResponseEntity<Object> suspendAlertSubscription(@PathVariable Long alertId){
        return consumerService.suspendAlertSubscriptionById(alertId);
    }
    @PutMapping("activate/alert_subscription/{alertId}")
    public ResponseEntity<Object> activateAlertSubscription(@PathVariable Long alertId){
        return consumerService.activateAlertSubscription(alertId);
    }

    @DeleteMapping("delete/consumer/alert/{alertId}")
    public ResponseEntity<Object> deleteAlert(@PathVariable long alertId){
        return consumerService.deleteAlert(alertId);
    }

    @PutMapping("update/db_connection/{watchlistId}")
    public ResponseEntity<Object> updateDBConnection(@PathVariable Long watchlistId, @RequestBody DBWatchlist dbWatchlist){
        return consumerService.updateDBConnection(watchlistId, dbWatchlist);
    }

    @PutMapping("update/recipient_group/{groupId}")
    public ResponseEntity<Object> updateRecipientGroup(@PathVariable Long groupId, @RequestBody RecepientGroup recepientGroup){
        return consumerService.updateRecipientGroup(groupId, recepientGroup);
    }

}
