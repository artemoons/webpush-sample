package com.artemoons.webpush.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Push message DTO.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class PushMessage {

    /**
     * Title field.
     */
    private final String title;

    /**
     * Body field.
     */
    private final String body;

}
