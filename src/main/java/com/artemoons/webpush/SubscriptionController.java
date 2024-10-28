package com.artemoons.webpush;

import com.artemoons.webpush.dto.PushMessage;
import com.artemoons.webpush.dto.Subscription;
import com.artemoons.webpush.dto.SubscriptionEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Subscription controller.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
public class SubscriptionController {
    /**
     * Message service.
     */
    private final MessageService messageService;
    /**
     * Server keys service.
     */
    private final ServerKeysService serverKeysService;

    /**
     * Constructor.
     *
     * @param msgService  message service
     * @param keysService server keys service
     */
    @Autowired
    public SubscriptionController(final MessageService msgService, final ServerKeysService keysService) {
        this.messageService = msgService;
        this.serverKeysService = keysService;
    }

    /**
     * Controller for obtaining public signing key.
     *
     * @return public signing key
     */
    @GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
    public byte[] publicSigningKey() {
        return serverKeysService.getPublicKeyUncompressed();
    }

    /**
     * Controller for subscribing.
     *
     * @param subscription subscription info
     */
    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(final @RequestBody Subscription subscription) {
        messageService.getSubscriberList().put(subscription.getEndpoint(), subscription);
    }

    /**
     * Controller for unsubscription.
     *
     * @param subscription subscription info
     */
    @PostMapping("/unsubscribe")
    public void unsubscribe(final @RequestBody SubscriptionEndpoint subscription) {
        messageService.getSubscriberList().remove(subscription.getEndpoint());
    }

    /**
     * Controller for checking subscription status.
     *
     * @param subscription subscription info
     * @return true if subscribed, otherwise false
     */
    @PostMapping("/isSubscribed")
    public boolean isSubscribed(final @RequestBody SubscriptionEndpoint subscription) {
        return messageService.getSubscriberList().containsKey(subscription.getEndpoint());
    }

    /**
     * Controller for sending push notification.
     *
     * @param message request payload
     * @return 200 OK if it's OK
     */
    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(final @RequestBody PushMessage message) {
        messageService.sendMessage(message);
        return ResponseEntity.ok("Message sent");
    }

}
