package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GetUserByIdUseCase {
  private final UserRepository userRepository;

  public UserResponseDto execute(UUID id) {
    log.info("Get user with id: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
    UserResponseDto dto = this.mapToDto(user);
    return dto;
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
