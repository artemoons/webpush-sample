package com.artemoons.webpush;

import com.artemoons.webpush.dto.PushMessage;
import com.artemoons.webpush.dto.Subscription;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:UtkinAK@cbr.ru">Artem Utkin</a>
 */
@Slf4j
@Getter
@Service
public class MessageService {

    public static final String SUBJECT_MAIL = "mailto:example@example.com";
    private final CryptoService cryptoService;
    private final ServerKeys serverKeys;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private Map<String, Subscription> subscriberList = new ConcurrentHashMap<>();

    @Autowired
    public MessageService(final CryptoService cryptoService,
                          final ServerKeys serverKeys,
                          final ObjectMapper objectMapper) {
        this.cryptoService = cryptoService;
        this.serverKeys = serverKeys;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void sendMessage(final PushMessage message) {
        if (subscriberList.isEmpty()) {
            log.warn("Subscribers list is empty, push messages won't be sent");
        } else {
            sendPushMessageToSubscribers(subscriberList, message);
        }
    }

    private void sendPushMessageToSubscribers(final Map<String, Subscription> subscribers,
                                              final Object message) {

        Set<String> failedSubscriptions = new HashSet<>();

        for (Subscription subscription : subscribers.values()) {
            try {
                byte[] result = cryptoService.encrypt(objectMapper.writeValueAsString(message),
                        subscription.getKeys().getP256dh(),
                        subscription.getKeys().getAuth(),
                        0);
                boolean remove = sendPushMessage(subscription, result);
                if (remove) {
                    failedSubscriptions.add(subscription.getEndpoint());
                }
            } catch (Exception ex) {
                log.error("Error occurred when sending push message", ex);
            }
        }
        failedSubscriptions.forEach(subscribers::remove);
    }

    /**
     * @return true if the subscription is no longer valid and can be removed, false if
     * everything is okay
     */
    private boolean sendPushMessage(Subscription subscription, byte[] body) {

        Algorithm jwtAlgorithm = Algorithm.ECDSA256(serverKeys.getPublicKey(), serverKeys.getPrivateKey());
        String origin;

        try {
            URL url = new URL(subscription.getEndpoint());
            origin = url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException ex) {
            log.error("Can't get endpoint for subscriber ", ex);
            return true;
        }

        Date expires = new Date(new Date().getTime() + 12 * 60 * 60 * 1000);

        String token = JWT.create()
                .withAudience(origin)
                .withExpiresAt(expires)
                .withSubject(SUBJECT_MAIL)
                .sign(jwtAlgorithm);

        URI endpointURI = URI.create(subscription.getEndpoint());

        HttpHeaders httpHeaders = prepareHeaders(token);
        HttpEntity<byte[]> entity = new HttpEntity<>(body, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HttpResponse> response = restTemplate.exchange(endpointURI,
                HttpMethod.POST,
                entity,
                HttpResponse.class);

        switch (response.getStatusCode().value()) {
            case 201 -> log.info("Push message successfully sent: {}", subscription.getEndpoint());
            case 404, 410 -> {
                log.warn("Subscription not found or gone: {}", subscription.getEndpoint());
                return true;
            }
            case 429 -> log.error("Too many requests: {}", entity);
            case 400 -> log.error("Invalid request: {}", entity);
            case 413 -> log.error("Payload size too large: {}", entity);
            default -> log.error("Unhandled status code: {} / {}", response.getStatusCode(), entity);
        }
        return false;
    }

    private HttpHeaders prepareHeaders(final String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.put("Content-Encoding", List.of("aes128gcm"));
        httpHeaders.put("TTL", List.of("180"));
        httpHeaders.put("Authorization", List.of("vapid t=" + token + ", k=" + serverKeys.getPublicKeyBase64()));
        return httpHeaders;
    }

}
