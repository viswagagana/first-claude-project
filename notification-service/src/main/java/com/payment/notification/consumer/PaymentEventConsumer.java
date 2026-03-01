package com.payment.notification.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.notification.domain.Notification;
import com.payment.notification.repository.NotificationRepository;
import com.payment.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String DLQ_TOPIC = "notification.events.dlq";

    @KafkaListener(topics = {"payment.events", "refund.events"}, groupId = "notification-service")
    public void consume(@Payload String message, @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventId = root.path("eventId").asText(null);
            if (eventId == null) eventId = key;
            if (notificationRepository.existsByEventId(eventId)) {
                log.debug("Duplicate event ignored: {}", eventId);
                return;
            }
            String type = root.path("type").asText(null);
            String accountId = root.path("accountId").asText();
            String amount = root.path("amount").asText();
            String currency = root.path("currency").asText();
            String subject = type + " - " + amount + " " + currency;
            String body = "Event: " + type + " for account " + accountId + ", amount " + amount + " " + currency;
            Notification n = Notification.create(eventId, accountId, "EMAIL", type, subject, body, "user@" + accountId + ".example.com");
            notificationRepository.save(n);
            notificationSender.send(n);
            n.setStatus("SENT");
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("Failed to process notification event, sending to DLQ: key={}", key, e);
            kafkaTemplate.send(DLQ_TOPIC, key, message);
        }
    }
}
