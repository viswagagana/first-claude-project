package com.payment.notification.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @Indexed(unique = true)
    private String eventId;

    @Indexed
    private String accountId;
    private String channel; // EMAIL, SMS
    private String type;   // PAYMENT_CAPTURED, REFUND_COMPLETED, etc.
    private String status; // PENDING, SENT, FAILED
    private String subject;
    private String body;
    private String recipient;
    private Instant createdAt;

    public static Notification create(String eventId, String accountId, String channel, String type, String subject, String body, String recipient) {
        Notification n = new Notification();
        n.setId(UUID.randomUUID().toString());
        n.setEventId(eventId);
        n.setAccountId(accountId);
        n.setChannel(channel);
        n.setType(type);
        n.setStatus("PENDING");
        n.setSubject(subject);
        n.setBody(body);
        n.setRecipient(recipient);
        n.setCreatedAt(Instant.now());
        return n;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
