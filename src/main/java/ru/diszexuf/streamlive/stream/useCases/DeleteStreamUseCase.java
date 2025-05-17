package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeleteStreamUseCase {
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;

    public void execute() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("User not found"));
        Stream stream = streamRepository.findByUserAndIsLiveTrue(user);
        stream.setIsLive(false);
        streamRepository.save(stream);
    }
} 