package ru.diszexuf.streamlive.old.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamDto {
    private UUID id;
    private UserGetRequest user;
    private String title;
    private String description;
    private String thumbnailUrl;
    private UUID streamKey;
    private Set<String> tags;
    private Boolean isLive;
    private Integer viewersCount;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
} 