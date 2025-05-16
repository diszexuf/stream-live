package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.stream.StreamRepository;

import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteStreamUseCase {
    private final StreamRepository streamRepository;

    public boolean execute(UUID id) {
        log.info("Deleting stream: {}", id);
        
        if (streamRepository.existsById(id)) {
            streamRepository.deleteById(id);
            return true;
        }
        return false;
    }
} 