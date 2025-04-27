package ru.diszexuf.streamlive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.diszexuf.streamlive.dto.UserDto;
import ru.diszexuf.streamlive.model.User;
import ru.diszexuf.streamlive.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

  private final UserRepository userRepository;

  public List<UserDto> getAllUsers() {
    log.info("Fetching all users");
    return userRepository.findAll().stream()
        .map(this::convertToDto)
        .collect(Collectors.toList());
  }

  public Optional<UserDto> getUserById(UUID id) {
    log.info("Fetching user by ID: {}", id);
    return userRepository.findById(id)
        .map(this::convertToDto);
  }

  public Optional<UserDto> getUserByUsername(String username) {
    log.info("Fetching user by username: {}", username);
    return userRepository.findByUsername(username)
        .map(this::convertToDto);
  }

  public UserDto createUser(User user) {
    log.info("Creating user: {}", user.getUsername());
    User savedUser = userRepository.save(user);
    return convertToDto(savedUser);
  }

  public Optional<UserDto> updateUser(UUID id, User updatedUser) {
    log.info("Updating user with ID: {}", id);
    return userRepository.findById(id)
        .map(user -> {
          user.setUsername(updatedUser.getUsername());
          user.setEmail(updatedUser.getEmail());
          user.setAvatarUrl(updatedUser.getAvatarUrl());
          user.setBio(updatedUser.getBio());
          return userRepository.save(user);
        })
        .map(this::convertToDto);
  }

  public boolean deleteUser(UUID id) {
    log.info("Deleting user with ID: {}", id);
    if (userRepository.existsById(id)) {
      userRepository.deleteById(id);
      return true;
    }
    return false;
  }

  public UserDto convertToDto(User user) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getPasswordHash(),
        user.getAvatarUrl(),
        user.getBio(),
        user.getFollowerCount(),
        user.getStreamKey().toString()
    );
  }

  // Простая проверка данных для входа (в реальном приложении здесь была бы проверка хэшей паролей)
  public Optional<UserDto> authenticateUser(String username, String password) {
    log.info("Authenticating user: {}", username);
    return userRepository.findByUsername(username)
        .filter(user -> user.getPasswordHash().equals(password))
        .map(this::convertToDto);
  }

  public void updateFollowersCount(UUID userId, int delta) {
    log.info("Updating followers count for user ID: {} by {}", userId, delta);
    userRepository.findById(userId).ifPresent(user -> {
      user.setFollowerCount(user.getFollowerCount() + delta);
      userRepository.save(user);
    });
  }
} 