package ru.diszexuf.streamlive.stream.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDto {
    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private UUID streamKey;
    private List<String> tags;
    private Boolean isLive;
    private Integer viewersCount;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
} 