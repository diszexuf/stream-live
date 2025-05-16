//package ru.diszexuf.streamlive.stream.useCases;
//
//import lombok.RequiredArgsConstructor;
//import ru.diszexuf.streamlive.common.UseCase;
//import ru.diszexuf.streamlive.stream.StreamMapper;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.stream.dto.StreamDto;
//
//import java.util.NoSuchElementException;
//import java.util.Optional;
//import java.util.UUID;
//
//@UseCase
//@RequiredArgsConstructor
//public class GetStreamByIdUseCase {
//    private final StreamRepository streamRepository;
//    private final StreamMapper streamMapper;
//
//    public StreamDto execute(UUID id) {
//        return streamRepository.findById(id)
//                .map(streamMapper::toStreamDto)
//                .orElseThrow(() -> new NoSuchElementException("Stream not found with id: " + id));
//    }
//
//    public Optional<StreamDto> executeOptional(UUID id) {
//        return streamRepository.findById(id)
//                .map(streamMapper::toStreamDto);
//    }
//}