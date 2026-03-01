package com.payment.notification.api;

import com.payment.notification.domain.Notification;
import com.payment.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification history")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/notifications")
    @Operation(summary = "List notifications by account")
    public List<NotificationDto> listByAccount(@RequestParam String accountId, @RequestParam(defaultValue = "50") int size) {
        return notificationRepository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, size)).stream()
            .map(NotificationDto::from)
            .toList();
    }

    public record NotificationDto(String id, String eventId, String type, String channel, String status, String createdAt) {
        static NotificationDto from(Notification n) {
            return new NotificationDto(n.getId(), n.getEventId(), n.getType(), n.getChannel(), n.getStatus(), n.getCreatedAt().toString());
        }
    }
}
