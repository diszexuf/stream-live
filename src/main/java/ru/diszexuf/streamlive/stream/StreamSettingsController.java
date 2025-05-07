package ru.diszexuf.streamlive.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stream/settings")
@RequiredArgsConstructor
@CrossOrigin
public class StreamSettingsController {
    
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateStreamSettings(@RequestBody Map<String, Object> settings) {
        // В реальном приложении здесь была бы проверка токена и сохранение настроек
        // Для демонстрации просто возвращаем те же настройки
        return ResponseEntity.ok(settings);
    }
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getStreamSettings() {
        // В реальном приложении здесь была бы проверка токена
        // Для демонстрации возвращаем заглушку с настройками
        Map<String, Object> settings = new HashMap<>();
        settings.put("defaultTitle", "Ежедневный стрим");
        settings.put("defaultCategory", "Игры");
        
        return ResponseEntity.ok(settings);
    }
} 