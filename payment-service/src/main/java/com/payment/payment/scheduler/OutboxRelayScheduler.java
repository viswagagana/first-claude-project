package com.payment.payment.scheduler;

import com.payment.payment.domain.OutboxEvent;
import com.payment.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_PAYMENT = "payment.events";
    private static final String TOPIC_REFUND = "refund.events";

    @Scheduled(fixedDelayString = "${app.outbox.relay-interval-ms:5000}")
    @Transactional
    public void relayOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findByPublishedAtIsNullOrderByIdAsc(org.springframework.data.domain.PageRequest.of(0, 50));
        for (OutboxEvent e : events) {
            try {
                String topic = e.getEventType().startsWith("Refund") ? TOPIC_REFUND : TOPIC_PAYMENT;
                kafkaTemplate.send(topic, e.getAggregateId(), e.getPayload()).get();
                e.setPublishedAt(Instant.now());
                outboxEventRepository.save(e);
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event id={}", e.getId(), ex);
                break;
            }
        }
    }
}
