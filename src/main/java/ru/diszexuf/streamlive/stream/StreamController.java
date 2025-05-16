//package ru.diszexuf.streamlive.stream;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import ru.diszexuf.streamlive.api.StreamsApi;
//import ru.diszexuf.streamlive.model.StreamRequestDto;
//import ru.diszexuf.streamlive.model.StreamResponseDto;
//import ru.diszexuf.streamlive.model.UpdateStreamKey200ResponseDto;
//import ru.diszexuf.streamlive.stream.dto.StreamDto;
//import ru.diszexuf.streamlive.stream.useCases.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/streams")
//@RequiredArgsConstructor
//@CrossOrigin
//public class StreamController implements StreamsApi {
//
//    private final GetAllStreamsUseCase getAllStreamsUseCase;
//    private final GetLiveStreamsUseCase getLiveStreamsUseCase;
//    private final GetStreamByIdUseCase getStreamByIdUseCase;
//    private final GetStreamsByUserUseCase getStreamsByUserUseCase;
//    private final SearchStreamsUseCase searchStreamsUseCase;
//    private final CreateStreamUseCase createStreamUseCase;
//    private final UpdateStreamUseCase updateStreamUseCase;
//    private final DeleteStreamUseCase deleteStreamUseCase;
//    private final StartStreamUseCase startStreamUseCase;
//    private final EndStreamUseCase endStreamUseCase;
//    private final ResetStreamKeyUseCase resetStreamKeyUseCase;
//
//    @Override
//    public ResponseEntity<StreamResponseDto> createStream(UUID userId, StreamRequestDto streamRequestDto) {
//        return ResponseEntity.ok(createStreamUseCase.execute(userId, streamRequestDto));
//    }
//
//    @Override
//    public ResponseEntity<Void> deleteStream(UUID streamId) {
//        deleteStreamUseCase.execute(streamId);
//        return ResponseEntity.ok().build();
//    }
//
//    @Override
//    public ResponseEntity<StreamResponseDto> endStream(UUID streamId) {
//        return ResponseEntity.ok(endStreamUseCase.execute(streamId));
//    }
//
//    @Override
//    public ResponseEntity<List<StreamResponseDto>> getAllStreams() {
//        return ResponseEntity.ok(getAllStreamsUseCase.execute());
//    }
//
//    @Override
//    public ResponseEntity<List<StreamResponseDto>> getLiveStreams() {
//        return ResponseEntity.ok(getLiveStreamsUseCase.execute());
//    }
//
//    @Override
//    public ResponseEntity<StreamResponseDto> getStreamById(UUID streamId) {
//        return ResponseEntity.ok(getStreamByIdUseCase.execute(streamId));
//    }
//
//    @Override
//    public ResponseEntity<List<StreamResponseDto>> getStreamsByUser(UUID id) {
//        return ResponseEntity.ok(getStreamsByUserUseCase.execute(id));
//    }
//
//    @Override
//    public ResponseEntity<UpdateStreamKey200ResponseDto> resetStreamKey(UUID streamId, UUID userId) {
//        return ResponseEntity.ok(resetStreamKeyUseCase.execute(streamId, userId));
//    }
//
//    @Override
//    public ResponseEntity<List<StreamResponseDto>> searchStreams(String query) {
//        return ResponseEntity.ok(searchStreamsUseCase.execute(query));
//    }
//
//    @Override
//    public ResponseEntity<StreamResponseDto> startStream(UUID streamId) {
//        return ResponseEntity.ok(startStreamUseCase.execute(streamId));
//    }
//
//    @Override
//    public ResponseEntity<StreamResponseDto> updateStream(UUID streamId, StreamRequestDto streamRequestDto) {
//        return ResponseEntity.ok(updateStreamUseCase.execute(streamId, streamRequestDto));
//    }
//}
