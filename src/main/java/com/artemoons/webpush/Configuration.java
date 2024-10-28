package com.artemoons.webpush;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class.
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "encryption.server")
public class Configuration {

    /**
     * Public key path.
     */
    private String publicKeyPath;

    /**
     * Private key path.
     */
    private String privateKeyPath;

}
