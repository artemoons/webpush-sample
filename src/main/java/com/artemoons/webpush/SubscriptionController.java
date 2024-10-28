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

@Slf4j
@RestController
@RequestMapping(value = "/api/v1")
public class SubscriptionController {
    private final MessageService messageService;
    private final ServerKeys serverKeys;

    @Autowired
    public SubscriptionController(final MessageService messageService, final ServerKeys serverKeys) {
        this.messageService = messageService;
        this.serverKeys = serverKeys;
    }

    @GetMapping(path = "/publicSigningKey", produces = "application/octet-stream")
    public byte[] publicSigningKey() {
        return serverKeys.getPublicKeyUncompressed();
    }

    @PostMapping("/subscribe")
    @ResponseStatus(HttpStatus.CREATED)
    public void subscribe(@RequestBody Subscription subscription) {
        messageService.getSubscriberList().put(subscription.getEndpoint(), subscription);
    }

    @PostMapping("/unsubscribe")
    public void unsubscribe(@RequestBody SubscriptionEndpoint subscription) {
        messageService.getSubscriberList().remove(subscription.getEndpoint());
    }

    @PostMapping("/isSubscribed")
    public boolean isSubscribed(@RequestBody SubscriptionEndpoint subscription) {
        return messageService.getSubscriberList().containsKey(subscription.getEndpoint());
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody PushMessage message) {
        messageService.sendMessage(message);
        return ResponseEntity.ok("Message sent");
    }

}
