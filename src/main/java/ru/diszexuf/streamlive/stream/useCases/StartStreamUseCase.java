package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.stream.StreamMapper;
import ru.diszexuf.streamlive.stream.StreamRepository;
import ru.diszexuf.streamlive.stream.dto.StreamDto;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StartStreamUseCase {
    private final StreamRepository streamRepository;
    private final StreamMapper streamMapper;

    public Optional<StreamDto> execute(UUID id) {
        log.info("Starting stream: {}", id);

        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setIsLive(true);
                    stream.setStartedAt(LocalDateTime.now());
                    stream.setViewersCount(0);
                    return streamRepository.save(stream);
                })
                .map(streamMapper::toStreamDto);
    }
}