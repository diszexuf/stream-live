package ru.diszexuf.streamlive.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.diszexuf.streamlive.service.StreamService;

import java.util.UUID;

@RestController
@RequestMapping("/api/stream/key")
@RequiredArgsConstructor
@CrossOrigin
public class StreamKeyController {
    
    private final StreamService streamService;
    
    @GetMapping
    public ResponseEntity<String> getStreamKey() {
        // В реальном приложении здесь была бы проверка токена
        // Для демонстрации возвращаем ключ стрима для тестового пользователя
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Здесь в реальном приложении должен быть запрос за ключом пользователя
        return ResponseEntity.ok("live_12345678_AbCdEfGhIjKlMnOpQrStUvWxYz");
    }
    
    @PostMapping("/reset")
    public ResponseEntity<String> resetStreamKey() {
        // В реальном приложении здесь была бы проверка токена
        // Для демонстрации обновляем ключ стрима для тестового пользователя
        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        // Здесь в реальном приложении должен быть запрос на обновление ключа пользователя
        return ResponseEntity.ok("live_" + System.currentTimeMillis() + "_AbCdEfGhIjKlMnOpQrStUvWxYz");
    }
} 