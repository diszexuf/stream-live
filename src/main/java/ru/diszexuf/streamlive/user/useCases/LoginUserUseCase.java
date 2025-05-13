package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@RequiredArgsConstructor
@Transactional
public class LoginUserUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserGetRequestDto execute(UserAuthRequestDto dto) {
    if (!userRepository.existsByUsername(dto.getUsername())) {
      throw new NoSuchElementException("User not found");
    }

    User user = userRepository.findByUsername(dto.getUsername()).orElseThrow(NoSuchElementException::new);
    if (!user.getPassword().equals(dto.getPassword())) {
      throw new NoSuchElementException("Wrong password");
    }
    return userMapper.mapToDto(user);
  }
}
