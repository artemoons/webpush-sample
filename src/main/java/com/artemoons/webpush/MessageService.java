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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message send service.
 *
 * @author <a href="mailto:github@eeel.ru">Artem Utkin</a>
 */
@Slf4j
@Getter
@Service
public class MessageService {

    /**
     * Subject mail.
     */
    public static final String SUBJECT_MAIL = "mailto:example@example.com";
    /**
     * Conversion coefficient.
     */
    public static final int COEFFICIENT = 12 * 60 * 60 * 1000;
    /**
     * Time to live.
     */
    public static final String TTL_TIME = "180";
    /**
     * Encoding type.
     */
    public static final String ENCODING = "aes128gcm";
    /**
     * Cryptographic service.
     */
    private final CryptoService cryptoService;
    /**
     * Server keys.
     */
    private final ServerKeysService serverKeysService;
    /**
     * JSON object mapper.
     */
    private final ObjectMapper objectMapper;
    /**
     * Subscribers list.
     */
    private Map<String, Subscription> subscriberList = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param cryptoSvc   cryptographic service
     * @param keysService server keys
     * @param jsonMapper  object mapper
     */
    @Autowired
    public MessageService(final CryptoService cryptoSvc,
                          final ServerKeysService keysService,
                          final ObjectMapper jsonMapper) {
        this.cryptoService = cryptoSvc;
        this.serverKeysService = keysService;
        this.objectMapper = jsonMapper;
    }

    /**
     * Method for sending push notifications.
     *
     * @param message input message
     */
    public void sendMessage(final PushMessage message) {
        if (subscriberList.isEmpty()) {
            log.warn("Subscribers list is empty, push messages won't be sent");
        } else {
            sendPushMessageToSubscribers(subscriberList, message);
        }
    }

    /**
     * Auxiliary method supporting message sending.
     *
     * @param subscribers subscribers map
     * @param payload     incoming message
     */
    private void sendPushMessageToSubscribers(final Map<String, Subscription> subscribers,
                                              final Object payload) {

        Set<String> failedSubscriptions = new HashSet<>();

        for (Subscription subscriber : subscribers.values()) {
            try {
                byte[] message = cryptoService.encrypt(objectMapper.writeValueAsString(payload),
                        subscriber.getKeys().getP256dh(),
                        subscriber.getKeys().getAuth(),
                        0);
                boolean remove = sendPushMessage(subscriber, message);
                if (remove) {
                    failedSubscriptions.add(subscriber.getEndpoint());
                }
            } catch (Exception ex) {
                log.error("Error occurred when sending push message", ex);
            }
        }
        failedSubscriptions.forEach(subscribers::remove);
    }

    /**
     * Auxiliary method supporting message sending.
     *
     * @param subscriber subscriber record
     * @param body       push message body
     * @return true if the subscription is no longer valid and can be removed, false if
     * everything is okay
     */
    private boolean sendPushMessage(final Subscription subscriber, final byte[] body) {

        Algorithm jwtAlgorithm = Algorithm.ECDSA256(serverKeysService.getPublicKey(),
                serverKeysService.getPrivateKey());
        String origin;

        try {
            URL url = new URL(subscriber.getEndpoint());
            origin = url.getProtocol() + "://" + url.getHost();
        } catch (MalformedURLException ex) {
            log.error("Can't get endpoint for subscriber ", ex);
            return true;
        }

        Date expires = new Date(new Date().getTime() + COEFFICIENT);

        String token = JWT.create()
                .withAudience(origin)
                .withExpiresAt(expires)
                .withSubject(SUBJECT_MAIL)
                .sign(jwtAlgorithm);

        URI endpointURI = URI.create(subscriber.getEndpoint());

        HttpHeaders httpHeaders = prepareHeaders(token);
        HttpEntity<byte[]> entity = new HttpEntity<>(body, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<HttpResponse> response = restTemplate.exchange(endpointURI,
                HttpMethod.POST,
                entity,
                HttpResponse.class);

        int responseStatusCode = response.getStatusCode().value();
        HttpStatus httpStatusCode = HttpStatus.valueOf(responseStatusCode);
        switch (httpStatusCode) {
            case CREATED -> log.info("Push message successfully sent: {}", subscriber.getEndpoint());
            case NOT_FOUND, GONE -> {
                log.warn("Subscription not found or gone: {}", subscriber.getEndpoint());
                return true;
            }
            case TOO_MANY_REQUESTS -> log.error("Too many requests: {}", entity);
            case BAD_REQUEST -> log.error("Invalid request: {}", entity);
            case PAYLOAD_TOO_LARGE -> log.error("Payload size too large: {}", entity);
            default -> log.error("Unhandled status code: {} / {}", response.getStatusCode(), entity);
        }
        return false;
    }

    /**
     * Auxiliary method for preparing HTTP headers.
     *
     * @param token authorization token
     * @return HttpHeaders
     */
    private HttpHeaders prepareHeaders(final String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        httpHeaders.put("Content-Encoding", List.of(ENCODING));
        httpHeaders.put("TTL", List.of(TTL_TIME));
        httpHeaders.put("Authorization", List.of("vapid t=" + token + ", k=" + serverKeysService.getPublicKeyBase64()));
        return httpHeaders;
    }

}
