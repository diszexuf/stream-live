//package ru.diszexuf.streamlive.stream.useCases;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import ru.diszexuf.streamlive.common.UseCase;
//import ru.diszexuf.streamlive.stream.Stream;
//import ru.diszexuf.streamlive.stream.StreamMapper;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.stream.dto.StreamDto;
//
//import java.util.HashSet;
//import java.util.Optional;
//import java.util.UUID;
//
//@UseCase
//@RequiredArgsConstructor
//@Slf4j
//@Transactional
//public class UpdateStreamUseCase {
//    private final StreamRepository streamRepository;
//    private final StreamMapper streamMapper;
//
//    public Optional<StreamDto> execute(UUID streamId, Stream updatedStream) {
//        log.info("Updating stream: {}", streamId);
//
//        return streamRepository.findById(streamId)
//                .map(stream -> {
//                    stream.setTitle(updatedStream.getTitle());
//                    if (updatedStream.getDescription() != null) {
//                        stream.setDescription(updatedStream.getDescription());
//                    }
//                    if (updatedStream.getThumbnailUrl() != null) {
//                        stream.setThumbnailUrl(updatedStream.getThumbnailUrl());
//                    }
//                    if (updatedStream.getTags() != null) {
//                        stream.setTags(new HashSet<>(updatedStream.getTags()));
//                    }
//                    return streamRepository.save(stream);
//                })
//                .map(streamMapper::toStreamDto);
//    }
//}