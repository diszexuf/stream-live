package ru.diszexuf.streamlive.stream;

import jakarta.persistence.*;
import lombok.*;
import ru.diszexuf.streamlive.common.CoreEntity;
import ru.diszexuf.streamlive.user.User;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "streams",
        indexes = {
                @Index(name = "idx_stream_user", columnList = "user_id"),
                @Index(name = "idx_stream_live", columnList = "is_live"),
                @Index(name = "idx_stream_title", columnList = "title")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_user"))
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @ElementCollection
    @CollectionTable(
        name = "stream_tags",
        joinColumns = @JoinColumn(name = "stream_id")
    )
    @Column(name = "tag")
    private Set<String> tags;

    @Column(name = "is_live", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLive;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "viewers_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewersCount = 0;
}