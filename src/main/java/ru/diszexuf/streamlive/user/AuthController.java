package ru.diszexuf.streamlive.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.diszexuf.streamlive.api.AuthApi;
import ru.diszexuf.streamlive.model.AuthResponseDto;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.user.useCases.LoginUserUseCase;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController implements AuthApi {
  private final LoginUserUseCase loginUserUseCase;

  @Override
  public ResponseEntity<AuthResponseDto> loginUser(@Valid UserAuthRequestDto userAuthRequestDto) {
    AuthResponseDto response = loginUserUseCase.execute(userAuthRequestDto);
    return ResponseEntity.ok(response);
  }
}
