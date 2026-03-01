package com.payment.notification.service;

import com.payment.notification.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Placeholder for email/SMS sending. In production, integrate with SendGrid, SES, Twilio, etc.
 */
@Service
@Slf4j
public class NotificationSender {

    public void send(Notification n) {
        if ("EMAIL".equals(n.getChannel())) {
            log.info("EMAIL to {}: {} - {}", n.getRecipient(), n.getSubject(), n.getBody());
            // TODO: send via SendGrid/SES
        } else if ("SMS".equals(n.getChannel())) {
            log.info("SMS to {}: {}", n.getRecipient(), n.getBody());
            // TODO: send via Twilio
        }
    }
}
