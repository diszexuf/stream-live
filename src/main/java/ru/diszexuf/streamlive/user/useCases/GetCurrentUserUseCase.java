package ru.diszexuf.streamlive.user.useCases;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetCurrentUserUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserResponseDto execute() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
    return userMapper.mapToDto(user);
  }
}
