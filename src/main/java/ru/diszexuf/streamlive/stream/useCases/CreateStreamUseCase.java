package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.StreamRequestDto;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamMapper;
import ru.diszexuf.streamlive.stream.StreamRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.NoSuchElementException;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateStreamUseCase {
  private final StreamRepository streamRepository;
  private final UserRepository userRepository;
  private final StreamMapper streamMapper;

  public StreamResponseDto execute(StreamRequestDto streamRequestDto) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("User not found"));

    Stream stream = Stream.builder()
        .user(user)
        .title(streamRequestDto.getTitle())
        .description(streamRequestDto.getDescription())
        .thumbnailUrl(streamRequestDto.getThumbnailUrl() != null ?
            streamRequestDto.getThumbnailUrl() : "https://picsum.photos/200/300")
        .streamKey(user.getStreamKey())
        .tags(streamRequestDto.getTags() != null ?
            new HashSet<>(streamRequestDto.getTags()) : new HashSet<>())
        .isLive(true)
        .viewersCount(0)
        .build();

    StreamResponseDto dto = mapToDto(streamRepository.save(stream));
    return dto;
  }

  public StreamResponseDto mapToDto(Stream stream) {
    return new StreamResponseDto()
        .id(stream.getId())
        .userId(stream.getUser().getId())
        .title(stream.getTitle())
        .description(stream.getDescription())
        .thumbnailUrl(stream.getThumbnailUrl())
        .streamKey(stream.getStreamKey())
        .tags(stream.getTags().stream().toList())
        .isLive(stream.getIsLive())
        .startedAt(stream.getStartedAt().atOffset(ZoneOffset.UTC))
        .viewerCount(stream.getViewersCount());
  }
}