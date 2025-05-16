package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserUpdateRequestDto;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateUserUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public void execute(UUID id, UserUpdateRequestDto dto) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("No such user with id: " + id));
//    user.setUsername(dto.setAvatarUrl(););
//    user.setBio(dto.getBio());
    userRepository.save(user);
  }
}
