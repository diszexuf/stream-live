package ru.diszexuf.streamlive.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.diszexuf.streamlive.dto.UserDto;
import ru.diszexuf.streamlive.model.User;
import ru.diszexuf.streamlive.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }
    
    public UserDto createUser(User user) {
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    public Optional<UserDto> updateUser(UUID id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(updatedUser.getUsername());
                    user.setEmail(updatedUser.getEmail());
                    user.setAvatar(updatedUser.getAvatar());
                    user.setBio(updatedUser.getBio());
                    return userRepository.save(user);
                })
                .map(this::convertToDto);
    }
    
    public boolean deleteUser(UUID id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                user.getBio(),
                user.getFollowersCount(),
                user.getCreatedAt()
        );
    }
    
    // Простая проверка данных для входа (в реальном приложении здесь была бы проверка хэшей паролей)
    public Optional<UserDto> authenticateUser(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(this::convertToDto);
    }
    
    public void updateFollowersCount(UUID userId, int delta) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setFollowersCount(user.getFollowersCount() + delta);
            userRepository.save(user);
        });
    }
} 