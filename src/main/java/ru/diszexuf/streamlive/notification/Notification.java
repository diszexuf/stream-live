package ru.diszexuf.streamlive.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import ru.diszexuf.streamlive.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User consumers;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "message")
    private String message;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;
}
