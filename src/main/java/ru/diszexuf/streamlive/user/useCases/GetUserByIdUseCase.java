package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.model.UserGetRequestDto;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;
import ru.diszexuf.streamlive.user.UserMapper;
import ru.diszexuf.streamlive.user.dto.UserGetRequest;

import java.util.NoSuchElementException;
import java.util.UUID;

@UseCase
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GetUserByIdUseCase {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserGetRequestDto execute(UUID id) {
      log.info("Get user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return userMapper.mapToDto(user);
    }
}
