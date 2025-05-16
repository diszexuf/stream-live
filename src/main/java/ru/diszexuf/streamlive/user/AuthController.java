package ru.diszexuf.streamlive.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.diszexuf.streamlive.api.AuthApi;
import ru.diszexuf.streamlive.model.AuthResponseDto;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.model.UserRegisterRequestDto;
import ru.diszexuf.streamlive.user.useCases.LoginUserUseCase;
import ru.diszexuf.streamlive.user.useCases.RegisterUserUseCase;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController implements AuthApi {
  private final LoginUserUseCase loginUserUseCase;
  private final RegisterUserUseCase registerUserUseCase;

  @Override
  public ResponseEntity<AuthResponseDto> registerUser(UserRegisterRequestDto userRegisterRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(registerUserUseCase.execute(userRegisterRequestDto));
  }

  @Override
  public ResponseEntity<AuthResponseDto> loginUser(@Valid UserAuthRequestDto userAuthRequestDto) {
    AuthResponseDto response = loginUserUseCase.execute(userAuthRequestDto);
    return ResponseEntity.ok(response);
  }
}
