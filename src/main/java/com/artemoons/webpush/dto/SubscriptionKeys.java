package com.artemoons.webpush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Subscription keys DTO.
 */
@Getter
@AllArgsConstructor
public class SubscriptionKeys {

    /**
     * P256H string.
     */
    private final String p256dh;

    /**
     * Authorization string.
     */
    private final String auth;
}
