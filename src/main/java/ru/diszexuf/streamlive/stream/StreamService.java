package ru.diszexuf.streamlive.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.diszexuf.streamlive.stream.dto.StreamDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamService {
    
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;

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
    
    public Optional<StreamDto> getStreamById(UUID id) {
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
    
    public List<StreamDto> searchStreams(String query) {
        log.info("Searching streams with query: {}", query);
        if (query.startsWith("#")) {
            String tag = query.substring(1).toLowerCase();
            return streamRepository.findByTag(tag).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return streamRepository.findByTitleContainingIgnoreCase(query).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }
    
    public Optional<StreamDto> createStream(Stream stream, UUID userId) {
        log.info("Creating stream for user: {}", userId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isPresent()) {
            stream.setUser(userOpt.get());
            stream.setStreamKey(generateStreamKey());
            stream.setViewersCount(0);
            Stream savedStream = streamRepository.save(stream);
            return Optional.of(convertToDto(savedStream));
        }
        
        return Optional.empty();
    }
    
    public Optional<StreamDto> updateStream(UUID id, Stream updatedStream) {
        log.info("Updating stream: {}", id);
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setTitle(updatedStream.getTitle());
                    stream.setDescription(updatedStream.getDescription());
                    stream.setThumbnailUrl(updatedStream.getThumbnailUrl());
                    stream.setTags(updatedStream.getTags());

                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public Optional<StreamDto> startStream(UUID id) {
        log.info("Starting stream: {}", id);
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setIsLive(true);
                    stream.setStartedAt(LocalDateTime.now());
                    stream.setViewersCount(0);
                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public Optional<StreamDto> endStream(UUID id) {
        log.info("Ending stream: {}", id);
        return streamRepository.findById(id)
                .map(stream -> {
                    stream.setIsLive(false);
                    stream.setEndedAt(LocalDateTime.now());
                    return streamRepository.save(stream);
                })
                .map(this::convertToDto);
    }
    
    public boolean deleteStream(UUID id) {
        log.info("Deleting stream: {}", id);
        if (streamRepository.existsById(id)) {
            streamRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public Optional<UUID> resetStreamKey(UUID streamId, UUID userId) {
        log.info("Resetting stream key for stream: {} and user: {}", streamId, userId);
        return streamRepository.findById(streamId)
                .filter(stream -> stream.getUser().getId().equals(userId))
                .map(stream -> {
                    UUID newKey = generateStreamKey();
                    stream.setStreamKey(newKey);
                    streamRepository.save(stream);
                    return newKey;
                });
    }

    private UUID generateStreamKey() {
        return UUID.randomUUID();
    }
    
    public StreamDto convertToDto(Stream stream) {
        return new StreamDto(
                stream.getId(),
                new UserGetRequest(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getThumbnailUrl(),
                stream.getStreamKey(),
                stream.getTags(),
                stream.getIsLive(),
                stream.getViewersCount(),
                stream.getStartedAt(),
                stream.getEndedAt()
        );
    }
} 