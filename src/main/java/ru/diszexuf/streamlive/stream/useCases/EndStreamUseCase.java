package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.stream.StreamMapper;
import ru.diszexuf.streamlive.stream.StreamRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EndStreamUseCase {
    private final StreamRepository streamRepository;
    private final StreamMapper streamMapper;

    public Optional<StreamDto> execute(UUID id) {
        log.info("Ending stream: {}", id);

        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setIsLive(false);
                    stream.setEndedAt(LocalDateTime.now());
                    return streamRepository.save(stream);
                })
                .map(streamMapper::toStreamDto);
    }
}