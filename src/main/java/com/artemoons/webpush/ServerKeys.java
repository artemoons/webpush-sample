package com.artemoons.webpush;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;


@Slf4j
@Getter
@Component
public class ServerKeys {
    private final Configuration configuration;
    private final CryptoService cryptoService;
    private ECPublicKey publicKey;
    private ECPrivateKey privateKey;
    private byte[] publicKeyUncompressed;
    private String publicKeyBase64;

    @Autowired
    public ServerKeys(final Configuration configuration, final CryptoService cryptoService) {
        this.configuration = configuration;
        this.cryptoService = cryptoService;
    }

    @PostConstruct
    private void initializeKeys() {
        Path serverPublicKeyFile = Paths.get(configuration.getPublicKeyPath());
        Path serverPrivateKeyFile = Paths.get(configuration.getPrivateKeyPath());

        if (Files.exists(serverPublicKeyFile) && Files.exists(serverPrivateKeyFile)) {
            try {
                byte[] appServerPublicKey = Files.readAllBytes(serverPublicKeyFile);
                byte[] appServerPrivateKey = Files.readAllBytes(serverPrivateKeyFile);

                publicKey = (ECPublicKey) cryptoService.convertX509ToECPublicKey(appServerPublicKey);
                privateKey = (ECPrivateKey) cryptoService.convertPKCS8ToECPrivateKey(appServerPrivateKey);

                publicKeyUncompressed = CryptoService.toUncompressedECPublicKey(publicKey);

                publicKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyUncompressed);
            } catch (IOException | InvalidKeySpecException e) {
                log.error("read files", e);
            }
        } else {
            try {
                KeyPair pair = cryptoService.getKeyPairGenerator().generateKeyPair();
                publicKey = (ECPublicKey) pair.getPublic();
                privateKey = (ECPrivateKey) pair.getPrivate();
                Files.write(serverPublicKeyFile, publicKey.getEncoded());
                Files.write(serverPrivateKeyFile, privateKey.getEncoded());

                publicKeyUncompressed = CryptoService.toUncompressedECPublicKey(publicKey);

                publicKeyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKeyUncompressed);
            } catch (IOException e) {
                log.error("write files", e);
            }
        }
    }

}
