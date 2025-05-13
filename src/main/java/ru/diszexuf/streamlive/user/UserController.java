package ru.diszexuf.streamlive.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.diszexuf.streamlive.api.UsersApi;
import ru.diszexuf.streamlive.model.UserAuthRequestDto;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.model.UserRegisterRequestDto;
import ru.diszexuf.streamlive.model.UserUpdateRequestDto;
import ru.diszexuf.streamlive.user.useCases.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController implements UsersApi {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetUserByUsernameUseCase getUserByUsernameUseCase;
  private final RegisterUserUseCase registerUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final UpdateUserStreamKeyUseCase updateUserStreamKeyUseCase;
  private final LoginUserUseCase loginUserUseCase;

  @Override
  public ResponseEntity<Void> deleteUser(UUID userId) {
    deleteUserUseCase.execute(userId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<UserGetRequestDto> getUserByUsername(String userUsername) {
    return ResponseEntity.ok(getUserByUsernameUseCase.execute(userUsername));
  }

  @Override
  public ResponseEntity<List<UserGetRequestDto>> getALlUsers() {
    return ResponseEntity.ok(getAllUsersUseCase.execute());
  }

  @Override
  public ResponseEntity<UserGetRequestDto> getUserById(UUID userId) {
    return ResponseEntity.ok(getUserByIdUseCase.execute(userId));
  }

  @Override
  public ResponseEntity<UserGetRequestDto> loginUser(UserAuthRequestDto userAuthRequestDto) {
    return ResponseEntity.ok(loginUserUseCase.execute(userAuthRequestDto));
  }

  @Override
  public ResponseEntity<UserGetRequestDto> registerUser(UserRegisterRequestDto userRegisterRequestDto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(registerUserUseCase.execute(userRegisterRequestDto));
  }

  @Override
  public ResponseEntity<Void> updateStreamKey(UUID userId) {
    updateUserStreamKeyUseCase.execute(userId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> updateUser(UUID userId, UserUpdateRequestDto userUpdateRequestDto) {
    updateUserUseCase.execute(userId, userUpdateRequestDto);
    return ResponseEntity.ok().build();
  }

}