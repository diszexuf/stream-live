package ru.diszexuf.streamlive.stream.useCases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.StreamMapper;
import ru.diszexuf.streamlive.stream.StreamRepository;

import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class SearchStreamsUseCase {
    private final StreamRepository streamRepository;
    private final StreamMapper streamMapper;

    public List<StreamResponseDto> execute(String query) {
        if (query.startsWith("#")) {
            String tag = query.substring(1).toLowerCase();
            return streamMapper.toStreamDtos(streamRepository.findByTag(tag));
        } else {
            return streamMapper.toStreamDtos(streamRepository.findByTitleContainingIgnoreCase(query));
        }
    }
}