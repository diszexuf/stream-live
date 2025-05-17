package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetUserByUsernameUseCase {
  private final UserRepository userRepository;

  public UserResponseDto execute(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("No such user with username: " + username));
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
