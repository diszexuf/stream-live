package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.UserMapper;

import java.util.List;

@UseCase
@Transactional
@AllArgsConstructor
@Slf4j
public class GetAllUsersUseCase {
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public List<UserResponseDto> execute() {
    log.info("Get all users");
    List<User> users = userRepository.findAll();
    return userMapper.mapToDtos(users);
  }

}
