package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserResponseDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UpdateUserUseCase {
  private final UserRepository userRepository;

  private static final String UPLOAD_DIR = "uploads/avatars/";

  public UserResponseDto execute(String email, MultipartFile avatar, String bio) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("User not found"));

    String avatarUrl = user.getAvatarUrl();
    if (avatar != null && !avatar.isEmpty()) {
      try {
        String extension = getFileExtension(avatar.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        Path path = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(path.getParent());
        Files.copy(avatar.getInputStream(), path);
        avatarUrl = "https://example.com/uploads/avatars/" + fileName;
        log.info("Avatar uploaded for user {}: {}", username, avatarUrl);
      } catch (IOException e) {
        log.error("Failed to upload avatar for user {}: {}", username, e.getMessage());
        throw new RuntimeException("Failed to upload avatar", e);
      }
    }

    user.setEmail(email);
    user.setBio(bio);
    user.setAvatarUrl(avatarUrl);

    User updatedUser = userRepository.save(user);
    return mapToDto(updatedUser);
  }

  private String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
  }

  public UserResponseDto mapToDto(User user) {
    return new UserResponseDto()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .avatarUrl(user.getAvatarUrl())
        .bio(user.getBio())
        .followerCount(user.getFollowerCount())
        .streamKey(user.getStreamKey());
  }
}
