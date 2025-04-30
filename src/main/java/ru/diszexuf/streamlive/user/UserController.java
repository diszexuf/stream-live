package ru.diszexuf.streamlive.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;
import ru.diszexuf.streamlive.user.dto.UserRegisterRequest;
import ru.diszexuf.streamlive.old.model.User;
import ru.diszexuf.streamlive.old.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserGetRequest>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserGetRequest> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserGetRequest> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<UserGetRequest> registerUser(@RequestBody UserRegisterRequest user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserGetRequest> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return userService.updateUser(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/login")
    public ResponseEntity<UserGetRequest> login(@RequestParam String username, @RequestParam String password) {
        return userService.authenticateUser(username, password)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @GetMapping("/profile")
    public ResponseEntity<UserGetRequest> getUserProfile() {
        return userService.getUserByUsername("streamMaster")
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    List<UserGetRequest> users = userService.getAllUsers();
                    if (!users.isEmpty()) {
                        return ResponseEntity.ok(users.get(0));
                    }
                    return ResponseEntity.notFound().build();
                });
    }
} 