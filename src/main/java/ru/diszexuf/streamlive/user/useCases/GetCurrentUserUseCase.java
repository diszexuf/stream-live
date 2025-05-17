package ru.diszexuf.streamlive.user.useCases;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@Transactional
@RequiredArgsConstructor
public class GetCurrentUserUseCase {
  private final UserRepository userRepository;

  public UserResponseDto execute() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
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
