package ru.diszexuf.streamlive.user.useCases;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ru.diszexuf.streamlive.common.UseCase;
import ru.diszexuf.streamlive.user.UserRepository;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CheckUsernameAvailabilityUseCase {

    private final UserRepository userRepository;

    public boolean execute(String username) {
        return userRepository.existsByUsername(username);
    }
}
