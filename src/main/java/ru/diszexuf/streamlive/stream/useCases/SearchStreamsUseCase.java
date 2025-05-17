package ru.diszexuf.streamlive.stream.useCases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamRepository;

import java.time.ZoneOffset;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SearchStreamsUseCase {
  private final StreamRepository streamRepository;

  public List<StreamResponseDto> execute(String query) {
    if (query.startsWith("#")) {
      String tag = query.substring(1).toLowerCase();
      return streamRepository.findByTag(tag).stream().map(this::mapToDto).toList();
    } else {
      return streamRepository.findByTitleContainingIgnoreCase(query).stream().map(this::mapToDto).toList();
    }
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