package com.artemoons.webpush;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "encryption.server")
public class Configuration {

    private String publicKeyPath;

    private String privateKeyPath;

}
