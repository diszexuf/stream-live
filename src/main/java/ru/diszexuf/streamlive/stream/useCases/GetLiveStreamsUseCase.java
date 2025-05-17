package ru.diszexuf.streamlive.stream.useCases;

import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamRepository;

import java.time.ZoneOffset;
import java.util.List;

@UseCase
@RequiredArgsConstructor
public class GetLiveStreamsUseCase {
  private final StreamRepository streamRepository;

  public List<StreamResponseDto> execute() {
    return streamRepository.findByIsLiveTrue()
        .stream()
        .map(this::mapToDto)
        .toList();
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