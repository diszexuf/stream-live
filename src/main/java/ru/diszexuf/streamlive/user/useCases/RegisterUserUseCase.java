package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.AuthResponseDto;
import ru.diszexuf.streamlive.model.UserRegisterRequestDto;
import ru.diszexuf.streamlive.security.JwtService;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.security.UserDetailsImpl;
import ru.diszexuf.streamlive.user.UserRepository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UseCase
@Transactional
@RequiredArgsConstructor
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponseDto execute(UserRegisterRequestDto dto) {
        boolean usernameExist = userRepository.existsByUsername(dto.getUsername());
        boolean emailExist = userRepository.existsByEmail(dto.getEmail());

        if (usernameExist && emailExist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username and email are already taken");
        } else if (usernameExist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username is already taken");
        } else if (emailExist) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email is already taken");
        }

        Set<String> authorities = Set.of("ROLE_USER");

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .streamKey(UUID.randomUUID())
                .enabled(true)
                .followerCount(0)
                .authorities(authorities)
                .build();

        userRepository.save(user);

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