//package ru.diszexuf.streamlive.stream.useCases;
//
//import lombok.RequiredArgsConstructor;
//import ru.diszexuf.streamlive.common.UseCase;
//import ru.diszexuf.streamlive.stream.StreamMapper;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.stream.dto.StreamDto;
//
//import java.util.List;
//
//@UseCase
//@RequiredArgsConstructor
//public class GetLiveStreamsUseCase {
//    private final StreamRepository streamRepository;
//    private final StreamMapper streamMapper;
//
//    public List<StreamDto> execute() {
//        return streamMapper.toStreamDtos(streamRepository.findByIsLiveTrue());
//    }
//}