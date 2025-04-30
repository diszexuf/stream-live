package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class DeleteUserUseCase {
  private final UserRepository userRepository;

  public void execute(String userId) {
    User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new NoSuchElementException("No such user with id: " + userId));
    userRepository.delete(user);
  }
}
