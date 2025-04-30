package ru.diszexuf.streamlive.old.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.diszexuf.streamlive.old.dto.StreamDto;
import ru.diszexuf.streamlive.old.model.Stream;
import ru.diszexuf.streamlive.old.service.StreamService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
@CrossOrigin
public class StreamController {
    
    private final StreamService streamService;
    
    @GetMapping
    public ResponseEntity<List<StreamDto>> getAllStreams() {
        return ResponseEntity.ok(streamService.getAllStreams());
    }
    
    @GetMapping("/live")
    public ResponseEntity<List<StreamDto>> getLiveStreams() {
        return ResponseEntity.ok(streamService.getLiveStreams());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<StreamDto> getStreamById(@PathVariable UUID id) {
        return streamService.getStreamById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StreamDto>> getStreamsByUser(@PathVariable UUID userId) {
        List<StreamDto> streams = streamService.getStreamsByUser(userId);
        return ResponseEntity.ok(streams);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<StreamDto>> searchStreams(@RequestParam String query) {
        return ResponseEntity.ok(streamService.searchStreams(query));
    }
    
    @PostMapping
    public ResponseEntity<StreamDto> createStream(
            @RequestBody Stream stream,
            @RequestParam UUID userId) {
        return streamService.createStream(stream, userId)
                .map(streamDto -> ResponseEntity.status(HttpStatus.CREATED).body(streamDto))
                .orElse(ResponseEntity.badRequest().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<StreamDto> updateStream(@PathVariable UUID id, @RequestBody Stream stream) {
        return streamService.updateStream(id, stream)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/start")
    public ResponseEntity<StreamDto> startStream(@PathVariable UUID id) {
        return streamService.startStream(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/{id}/end")
    public ResponseEntity<StreamDto> endStream(@PathVariable UUID id) {
        return streamService.endStream(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStream(@PathVariable UUID id) {
        if (streamService.deleteStream(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/{id}/reset-key")
    public ResponseEntity<UUID> resetStreamKey(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        return streamService.resetStreamKey(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
