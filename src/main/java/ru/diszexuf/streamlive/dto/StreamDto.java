package ru.diszexuf.streamlive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDto {
    private UUID id;
    private UserDto user;
    private String title;
    private String description;
    private String thumbnail;
    private UUID streamKey;
    private CategoryDto category;
    private boolean isLive;
    private LocalDateTime startedAt;
} 