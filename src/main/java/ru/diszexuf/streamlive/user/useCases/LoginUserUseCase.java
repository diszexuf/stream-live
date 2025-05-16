package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.AuthResponseDto;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.security.JwtService;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.security.UserDetailsImpl;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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

    UserDetails userDetails = new UserDetailsImpl(
        user.getUsername(),
        user.getPassword(),
        user.getAuthorities().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList())
    );

    String token = jwtService.generateToken(userDetails);

    return new AuthResponseDto().token(token);
  }
}
