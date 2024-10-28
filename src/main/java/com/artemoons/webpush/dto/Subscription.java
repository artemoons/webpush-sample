package com.artemoons.webpush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Subscription DTO.
 */
@Getter
@Setter
@AllArgsConstructor
public class Subscription {

    /**
     * Endpoint.
     */
    private final String endpoint;

    /**
     * Expiration time.
     */
    private final Long expirationTime;

    /**
     * Subscription keys.
     */
    private final SubscriptionKeys keys;

}
