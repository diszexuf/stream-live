package ru.diszexuf.streamlive.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.diszexuf.streamlive.api.UsersApi;
import ru.diszexuf.streamlive.model.RegenerateCurrentUserStreamKey200ResponseDto;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.model.UserUpdateRequestDto;
import ru.diszexuf.streamlive.user.useCases.ResetStreamKeyUseCase;
import ru.diszexuf.streamlive.user.useCases.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class UserController implements UsersApi {

  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetUserByUsernameUseCase getUserByUsernameUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final GetCurrentUserUseCase getCurrentUserUseCase;
  private final ResetStreamKeyUseCase resetStreamKeyUseCase;

  @Override
  public ResponseEntity<RegenerateCurrentUserStreamKey200ResponseDto> regenerateCurrentUserStreamKey() {
    return ResponseEntity.ok(resetStreamKeyUseCase.execute());
  }

  @Override
  public ResponseEntity<Void> deleteUser(UUID id) {
    deleteUserUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<List<UserResponseDto>> getAllUsers() {
    return ResponseEntity.ok(getAllUsersUseCase.execute());
  }

  @Override
  public ResponseEntity<UserResponseDto> getCurrentUserProfile() {
    return ResponseEntity.ok(getCurrentUserUseCase.execute());
  }

  @Override
  public ResponseEntity<UserResponseDto> getUserById(UUID id) {
    return ResponseEntity.ok(getUserByIdUseCase.execute(id));
  }

  @Override
  public ResponseEntity<UserResponseDto> getUserByUsername(String userUsername) {
    return ResponseEntity.ok(getUserByUsernameUseCase.execute(userUsername));
  }

  @Override
  public ResponseEntity<Void> updateUser(UserUpdateRequestDto userUpdateRequestDto) {
    updateUserUseCase.execute(userUpdateRequestDto);
    return ResponseEntity.ok().build();
  }

}