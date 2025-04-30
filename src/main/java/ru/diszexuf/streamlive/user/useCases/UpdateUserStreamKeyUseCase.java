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

  public String execute(String id) {
    User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new NoSuchElementException("No such user with id: " + id));
    UUID newStreamKey = UUID.randomUUID();
    user.setStreamKey(newStreamKey);
    userRepository.save(user);
    return newStreamKey.toString(); // todo: gen alg of stream key
  }
}
