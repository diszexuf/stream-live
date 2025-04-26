package ru.diszexuf.streamlive.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.diszexuf.streamlive.dto.StreamDto;
import ru.diszexuf.streamlive.model.Category;
import ru.diszexuf.streamlive.model.Stream;
import ru.diszexuf.streamlive.model.User;
import ru.diszexuf.streamlive.repository.CategoryRepository;
import ru.diszexuf.streamlive.repository.StreamRepository;
import ru.diszexuf.streamlive.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreamService {
    
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    
    public List<StreamDto> getAllStreams() {
        return streamRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<StreamDto> getLiveStreams() {
        return streamRepository.findByIsLiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<StreamDto> getStreamById(Long id) {
        return streamRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public List<StreamDto> getStreamsByUser(UUID userId) {
        return userRepository.findById(userId)
                .map(user -> streamRepository.findByUser(user).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    
    public List<StreamDto> getStreamsByCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(category -> streamRepository.findByCategory(category).stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }
    
    public Optional<StreamDto> createStream(Stream stream, UUID userId, Long categoryId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        
        if (userOpt.isPresent() && categoryOpt.isPresent()) {
            stream.setUser(userOpt.get());
            stream.setCategory(categoryOpt.get());
            stream.setStreamKey(generateStreamKey(userId.toString()));
            Stream savedStream = streamRepository.save(stream);
            return Optional.of(convertToDto(savedStream));
        }
        
        return Optional.empty();
    }
    
    public Optional<StreamDto> updateStream(Long id, Stream updatedStream) {
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setTitle(updatedStream.getTitle());
                    stream.setDescription(updatedStream.getDescription());
                    stream.setThumbnail(updatedStream.getThumbnail());
                    stream.setPrivate(updatedStream.isPrivate());
                    
                    if (updatedStream.getCategory() != null) {
                        categoryRepository.findById(updatedStream.getCategory().getId())
                                .ifPresent(stream::setCategory);
                    }
                    
                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public Optional<StreamDto> startStream(Long id) {
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setLive(true);
                    stream.setStartedAt(LocalDateTime.now());
                    stream.setViewersCount(0);
                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public Optional<StreamDto> endStream(Long id) {
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setLive(false);
                    stream.setEndedAt(LocalDateTime.now());
                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public boolean deleteStream(Long id) {
        if (streamRepository.existsById(id)) {
            streamRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public Optional<String> resetStreamKey(Long streamId, UUID userId) {
        return streamRepository.findById(streamId)
                .filter(stream -> stream.getUser().getId().equals(userId))
                .map(stream -> {
                    String newKey = generateStreamKey(userId.toString());
                    stream.setStreamKey(newKey);
                    streamRepository.save(stream);
                    return newKey;
                });
    }
    
    public Optional<StreamDto> updateViewerCount(Long streamId, int delta) {
        return streamRepository.findById(streamId)
                .map(stream -> {
                    if (stream.isLive()) {
                        int newCount = Math.max(0, stream.getViewersCount() + delta);
                        stream.setViewersCount(newCount);
                        
                        // Также обновляем счетчик зрителей в категории
                        if (stream.getCategory() != null) {
                            categoryService.updateViewersCount(stream.getCategory().getId(), delta);
                        }
                        
                        return streamRepository.save(stream);
                    }
                    return stream;
                })
                .map(this::convertToDto);
    }
    
    private String generateStreamKey(String userId) {
        // Простая реализация для демо. В реальности нужен более надежный способ
        return "stream_" + userId + "_" + System.currentTimeMillis();
    }
    
    public StreamDto convertToDto(Stream stream) {
        return new StreamDto(
                stream.getId(),
                userService.convertToDto(stream.getUser()),
                stream.getTitle(),
                stream.getDescription(),
                stream.getThumbnail(),
                categoryService.convertToDto(stream.getCategory()),
                stream.isLive(),
                stream.getViewersCount(),
                stream.isPrivate(),
                stream.getStartedAt()
        );
    }
} 