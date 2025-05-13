package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
public class UpdateUserStreamKeyUseCase {
  private final UserRepository userRepository;

  public void execute(UUID id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No such user with id: " + id));
    UUID newStreamKey = UUID.randomUUID();      // todo: gen alg of stream key
    user.setStreamKey(newStreamKey);
    userRepository.save(user);
  }
}
