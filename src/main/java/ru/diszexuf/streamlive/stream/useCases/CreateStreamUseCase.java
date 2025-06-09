package ru.diszexuf.streamlive.stream.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CreateStreamUseCase {
  private final StreamRepository streamRepository;
  private final UserRepository userRepository;

  private static final String UPLOAD_DIR = "uploads/thumbnails/";

  public StreamResponseDto execute(String title, String description, MultipartFile thumbnailUrl) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new NoSuchElementException("User not found"));

    String thumbnail;
    if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
      try {
        String extension = getFileExtension(thumbnailUrl.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;
        Path path = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(path.getParent());
        Files.copy(thumbnailUrl.getInputStream(), path);
        thumbnail = "http://127.0.0.1:8080/uploads/thumbnails/" + fileName;
      } catch (IOException e) {
        throw new RuntimeException("Failed to upload thumbnail", e);
      }
    } else {
      thumbnail = "https://picsum.photos/200/300";
    }

    Stream stream = Stream.builder()
        .user(user)
        .title(title)
        .description(description)
        .thumbnailUrl(thumbnail)
        .startedAt(LocalDateTime.now())
//        .tags(tags != null ?
//            new HashSet<>(tags) : new HashSet<>())
        .isLive(true)
        .viewersCount(0)
        .build();

    return mapToDto(streamRepository.save(stream));
  }

  private String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) {
      return "";
    }
    return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
  }

  public StreamResponseDto mapToDto(Stream stream) {
    return new StreamResponseDto()
        .id(stream.getId())
        .userId(stream.getUser().getId())
        .title(stream.getTitle())
        .description(stream.getDescription())
        .thumbnailUrl(stream.getThumbnailUrl())
        .streamKey(stream.getUser().getStreamKey())
//        .tags(stream.getTags().stream().toList())
        .isLive(stream.getIsLive())
        .viewerCount(stream.getViewersCount());
  }
}