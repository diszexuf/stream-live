package ru.diszexuf.streamlive.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.diszexuf.streamlive.api.StreamsApi;
import ru.diszexuf.streamlive.model.StreamResponseDto;
import ru.diszexuf.streamlive.stream.useCases.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
@Slf4j
public class StreamController implements StreamsApi {

    private final CreateStreamUseCase createStreamUseCase;
    private final DeleteStreamUseCase deleteStreamUseCase;
    private final GetAllStreamsUseCase getAllStreamsUseCase;
    private final GetLiveStreamsUseCase getLiveStreamsUseCase;
    private final GetStreamByIdUseCase getStreamByIdUseCase;
    private final GetStreamsByUserUseCase getStreamsByUserUseCase;
    private final SearchStreamsUseCase searchStreamsUseCase;
    private final UpdateStreamUseCase updateStreamUseCase;

    @Override
    public ResponseEntity<StreamResponseDto> createStream(String title, String description, MultipartFile thumbnailUrl) {
        return ResponseEntity.ok(createStreamUseCase.execute(title, description, thumbnailUrl));
    }

    @Override
    public ResponseEntity<Void> endStream() {
        deleteStreamUseCase.execute();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<StreamResponseDto>> getAllStreams() {
        return ResponseEntity.ok(getAllStreamsUseCase.execute());
    }

    @Override
    public ResponseEntity<List<StreamResponseDto>> getLiveStreams() {
        var streams = getLiveStreamsUseCase.execute();
        return ResponseEntity.ok(streams);
    }

    @Override
    public ResponseEntity<StreamResponseDto> getStreamById(UUID streamId) {
        return ResponseEntity.ok(getStreamByIdUseCase.execute(streamId));
    }

    @Override
    public ResponseEntity<List<StreamResponseDto>> getStreamsByUser(UUID userId) {
        return ResponseEntity.ok(getStreamsByUserUseCase.execute(userId));
    }

    @Override
    public ResponseEntity<List<StreamResponseDto>> searchStreams(String query) {
        return ResponseEntity.ok(searchStreamsUseCase.execute(query));
    }

    @Override
    public ResponseEntity<StreamResponseDto> updateStream(String title, String description, MultipartFile thumbnailUrl) {
        return ResponseEntity.ok(updateStreamUseCase.execute(title, description, thumbnailUrl));
    }
}
