package com.artemoons.webpush.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubscriptionKeys {

    private final String p256dh;

    private final String auth;
}