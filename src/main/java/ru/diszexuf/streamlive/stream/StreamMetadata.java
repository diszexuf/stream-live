package ru.diszexuf.streamlive.stream;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "stream_metadata")
@Entity
@Data
public class StreamMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "resolution")
    @Enumerated(EnumType.STRING)
    private Resolution resolution;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "language")
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
