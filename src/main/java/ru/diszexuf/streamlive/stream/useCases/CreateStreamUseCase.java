//package ru.diszexuf.streamlive.stream.useCases;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import ru.diszexuf.streamlive.common.UseCase;
//import ru.diszexuf.streamlive.model.StreamRequestDto;
//import ru.diszexuf.streamlive.model.StreamResponseDto;
//import ru.diszexuf.streamlive.stream.Stream;
//import ru.diszexuf.streamlive.stream.StreamMapper;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.user.UserRepository;
//
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.UUID;
//
//@UseCase
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class CreateStreamUseCase {
//    private final StreamRepository streamRepository;
//    private final UserRepository userRepository;
//    private final StreamMapper streamMapper;
//
//    public Optional<StreamResponseDto> execute(UUID userId, StreamRequestDto streamRequestDto) {
//        log.info("Creating stream for user: {}", userId);
//
//        return userRepository.findById(userId)
//                .map(user -> {
//                    Stream stream = Stream.builder()
//                            .user(user)
//                            .title(streamRequestDto.getTitle())
//                            .description(streamRequestDto.getDescription())
//                            .thumbnailUrl(streamRequestDto.getThumbnailUrl() != null ?
//                                streamRequestDto.get() : "default-thumbnail.jpg")
//                            .streamKey(generateStreamKey())
//                            .tags(streamRequestDto.getTags() != null ?
//                                    new HashSet<>(streamRequestDto.getTags()) : new HashSet<>())
//                            .isLive(false)
//                            .viewersCount(0)
//                            .build();
//                    return streamRepository.save(stream);
//                })
//                .map(streamMapper::toStreamDto);
//    }
//
//    private UUID generateStreamKey() {
//        return UUID.randomUUID();
//    }
//}