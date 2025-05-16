package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.AuthResponseDto;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.security.JwtService;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;

@UseCase
@RequiredArgsConstructor
@Transactional
public class LoginUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthResponseDto execute(UserAuthRequestDto dto) {
    User user = userRepository.findByUsername(dto.getUsername())
        .orElseThrow(() -> new NoSuchElementException("User not found"));

    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
      throw new NoSuchElementException("Wrong password");
    }

    String token = jwtService.generateToken(user);
    return new AuthResponseDto().token(token);
  }
}
