package ru.diszexuf.streamlive.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

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

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", nullable = false, length = 255)
    private String thumbnailUrl;

    @Column(name = "stream_key", unique = true, nullable = false, updatable = false, length = 64)
    private String streamKey;

    @ElementCollection
    @CollectionTable(
        name = "stream_tags",
        joinColumns = @JoinColumn(name = "stream_id")
    )
    @Column(name = "tag", length = 50)
    private Set<String> tags = new HashSet<>();

    @Column(name = "is_live", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLive;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "viewers_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewersCount = 0;

    public Stream(UUID id, User user, String title, String description, String thumbnailUrl, 
                 String streamKey, Set<String> tags, Boolean isLive, Integer viewersCount,
                 LocalDateTime startedAt, LocalDateTime endedAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.streamKey = streamKey;
        this.tags = tags != null ? tags : new HashSet<>();
        this.isLive = isLive;
        this.viewersCount = viewersCount;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    @Override
    public UUID getId() {
        return id;
    }
}