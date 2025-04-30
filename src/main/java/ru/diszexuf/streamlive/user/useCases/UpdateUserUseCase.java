package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;
import ru.diszexuf.streamlive.user.dto.UserUpdateRequest;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateUserUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public UserGetRequest execute(String id, UserUpdateRequest dto) throws RuntimeException {
    if (userRepository.existsByUsername(dto.getUsername())) {
      throw new RuntimeException("User with username is already exists");
    }

    User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new NoSuchElementException("No such user with id: " + id));
    user.setUsername(dto.getUsername());
    user.setBio(dto.getBio());
    return userMapper.mapToDto(userRepository.save(user));
  }
}
