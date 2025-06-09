package ru.diszexuf.streamlive.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.diszexuf.streamlive.api.UsersApi;
import ru.diszexuf.streamlive.model.RegenerateCurrentUserStreamKey200ResponseDto;
import ru.diszexuf.streamlive.model.UserResponseDto;
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
    private final CheckEmailAvailabilityUseCase checkEmailAvailabilityUseCase;
    private final CheckUsernameAvailabilityUseCase checkUsernameAvailabilityUseCase;

    @Override
    public ResponseEntity<Boolean> checkEmailAvailability(String email) {
        return ResponseEntity.ok(!checkEmailAvailabilityUseCase.execute(email));
    }

    @Override
    public ResponseEntity<Boolean> checkUsernameAvailability(String username) {
        return ResponseEntity.ok(!checkUsernameAvailabilityUseCase.execute(username));
    }

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
    public ResponseEntity<UserResponseDto> updateUser(String email, MultipartFile avatarUrl, String bio) {
        return ResponseEntity.ok(updateUserUseCase.execute(email, avatarUrl, bio));
    }
}