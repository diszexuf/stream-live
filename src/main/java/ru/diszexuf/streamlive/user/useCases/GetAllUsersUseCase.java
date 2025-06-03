package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.List;

@UseCase
@Transactional
@AllArgsConstructor
@Slf4j
public class GetAllUsersUseCase {
  private final UserRepository userRepository;

  public List<UserResponseDto> execute() {
    log.info("Get all users");
    List<User> users = userRepository.findAll();
      return users.stream().map(this::mapToDto).toList();
  }

  public UserResponseDto mapToDto(User user) {
    return new UserResponseDto()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .avatarUrl(user.getAvatarUrl())
        .bio(user.getBio())
        .followerCount(user.getFollowerCount())
        .streamKey(user.getStreamKey());
  }
}
