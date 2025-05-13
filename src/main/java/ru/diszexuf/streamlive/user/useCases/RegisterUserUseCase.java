package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.model.UserRegisterRequestDto;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;
import ru.diszexuf.streamlive.user.dto.UserRegisterRequest;

import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
public class RegisterUserUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserGetRequestDto execute(UserRegisterRequestDto dto) {
    User user = User.builder()
            .username(dto.getUsername())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .followerCount(0)
            .streamKey(UUID.randomUUID())
            .build();
    return userMapper.mapToDto(userRepository.save(user));
  }
}
