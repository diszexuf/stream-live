package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;

import java.util.NoSuchElementException;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetUserByUsernameUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserGetRequestDto execute(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("No such user with username: " + username));
    return userMapper.mapToDto(user);
  }
}
