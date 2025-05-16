//package ru.diszexuf.streamlive.stream.useCases;
//
//import lombok.RequiredArgsConstructor;
//import ru.diszexuf.streamlive.common.UseCase;
//import ru.diszexuf.streamlive.stream.StreamMapper;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.stream.dto.StreamDto;
//import ru.diszexuf.streamlive.user.UserRepository;
//
//import java.util.List;
//import java.util.UUID;
//
//@UseCase
//@RequiredArgsConstructor
//public class GetStreamsByUserUseCase {
//    private final StreamRepository streamRepository;
//    private final UserRepository userRepository;
//    private final StreamMapper streamMapper;
//
//    public List<StreamDto> execute(UUID userId) {
//        return userRepository.findById(userId)
//                .map(user -> streamMapper.toStreamDtos(streamRepository.findByUser(user)))
//                .orElse(List.of());
//    }
//}