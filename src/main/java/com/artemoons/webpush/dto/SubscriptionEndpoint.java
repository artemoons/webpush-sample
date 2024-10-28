package com.artemoons.webpush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Subscription endpoint DTO.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionEndpoint {

    /**
     * Endpoint URL.
     */
    private String endpoint;

}
