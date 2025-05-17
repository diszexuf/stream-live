package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserUpdateRequestDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateUserUseCase {
  private final UserRepository userRepository;

  public void execute(UserUpdateRequestDto dto) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("User not found"));
    user.setBio(dto.getBio().get());
    user.setAvatarUrl(dto.getAvatarUrl().toString());
    userRepository.save(user);
  }
}
