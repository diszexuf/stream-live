package ru.diszexuf.streamlive.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;
import ru.diszexuf.streamlive.user.dto.UserRegisterRequest;
import ru.diszexuf.streamlive.user.dto.UserUpdateRequest;
import ru.diszexuf.streamlive.user.useCases.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
  private final GetAllUsersUseCase getAllUsersUseCase;
  private final GetUserByIdUseCase getUserByIdUseCase;
  private final GetUserByUsernameUseCase getUserByUsernameUseCase;
  private final RegisterUserUseCase registerUserUseCase;
  private final UpdateUserUseCase updateUserUseCase;
  private final DeleteUserUseCase deleteUserUseCase;
  private final UpdateUserStreamKeyUseCase updateUserStreamKeyUseCase;

  @GetMapping
  public ResponseEntity<List<UserGetRequest>> getAllUsers() {
    return ResponseEntity.ok(getAllUsersUseCase.execute());
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserGetRequest> getUserById(@PathVariable String id) {
    return ResponseEntity.ok(getUserByIdUseCase.execute(id));
  }

  @GetMapping("/username/{username}")
  public ResponseEntity<UserGetRequest> getUserByUsername(@PathVariable String username) {
    return ResponseEntity.ok(getUserByUsernameUseCase.execute(username));
  }

  @PostMapping
  public ResponseEntity<UserGetRequest> registerUser(@RequestBody UserRegisterRequest dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(registerUserUseCase.execute(dto));
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserGetRequest> updateUser(@PathVariable String id, @RequestBody UserUpdateRequest dto) {
    try {
      UserGetRequest userGetRequest = updateUserUseCase.execute(id, dto);
      return ResponseEntity.ok(userGetRequest);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    deleteUserUseCase.execute(id);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @PutMapping("/{id}/streamkey")
  public ResponseEntity<String> updateStreamKey(@PathVariable String id) {
    return ResponseEntity.ok(updateUserStreamKeyUseCase.execute(id));
  }

  @PostMapping("/login")
  public ResponseEntity<UserGetRequest> login(@RequestParam String username, @RequestParam String password) {
    return ResponseEntity.ok(new UserGetRequest());
  }

}