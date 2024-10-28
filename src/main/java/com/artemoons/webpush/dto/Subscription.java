package com.artemoons.webpush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Subscription {

    private final String endpoint;

    private final Long expirationTime;

    public final SubscriptionKeys keys;

}
