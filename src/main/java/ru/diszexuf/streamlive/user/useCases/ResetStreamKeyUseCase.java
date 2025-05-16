package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.RegenerateCurrentUserStreamKey200ResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResetStreamKeyUseCase {
    private final UserRepository userRepository;

    public RegenerateCurrentUserStreamKey200ResponseDto execute() {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();

      User user = userRepository.findByUsername(username)
          .orElseThrow(() -> new NoSuchElementException("No such user"));
      user.setStreamKey(UUID.randomUUID());
      userRepository.save(user);
      return new RegenerateCurrentUserStreamKey200ResponseDto().newStreamKey(user.getStreamKey());
    }
}