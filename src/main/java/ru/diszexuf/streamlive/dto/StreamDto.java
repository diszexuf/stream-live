package ru.diszexuf.streamlive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDto {
    private UUID id;
    private UserDto user;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String streamKey;
    private Set<String> tags;
    private Boolean isLive;
    private Integer viewersCount;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
} 