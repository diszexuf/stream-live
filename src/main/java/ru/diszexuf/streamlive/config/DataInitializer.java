package ru.diszexuf.streamlive.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.diszexuf.streamlive.stream.Stream;
import ru.diszexuf.streamlive.stream.StreamRepository;
import ru.diszexuf.streamlive.user.Follow;
import ru.diszexuf.streamlive.user.FollowRepository;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final FollowRepository followRepository;
  private final StreamRepository streamRepository;

  @Override
  @Transactional
  public void run(String... args) {
    // Создание пользователей
    User alice = createUser("alice", "alice@example.com");
    User bob = createUser("bob", "bob@example.com");
    User charlie = createUser("charlie", "charlie@example.com");

    userRepository.saveAll(List.of(alice, bob, charlie));

    // Подписки
    followRepository.save(Follow.builder()
        .follower(alice)
        .following(bob)
        .build());

    followRepository.save(Follow.builder()
        .follower(charlie)
        .following(alice)
        .build());

    // Стримы Alice
    streamRepository.save(Stream.builder()
        .user(alice)
        .title("Alice's Cooking Stream")
        .description("Cooking delicious food live!")
        .thumbnailUrl("https://picsum.photos/200/300")
        .streamKey(UUID.randomUUID())
        .tags(Set.of("cooking", "live", "fun"))
        .isLive(true)
        .startedAt(LocalDateTime.now().minusMinutes(10))
        .viewersCount(42)
        .build());

    streamRepository.save(Stream.builder()
        .user(alice)
        .title("Quiet Stream")
        .description("Just chilling")
        .thumbnailUrl("https://picsum.photos/200/300")
        .streamKey(UUID.randomUUID())
        .tags(Set.of()) // без тегов
        .isLive(false)
        .endedAt(LocalDateTime.now().minusHours(2))
        .viewersCount(3)
        .build());

    // Стримы Bob
    streamRepository.save(Stream.builder()
        .user(bob)
        .title("Gaming with Bob")
        .description("Let's play together!")
        .thumbnailUrl("https://picsum.photos/200/300")
        .streamKey(UUID.randomUUID())
        .tags(Set.of("gaming", "fps"))
        .isLive(true)
        .startedAt(LocalDateTime.now().minusMinutes(20))
        .viewersCount(10)
        .build());
  }

  private User createUser(String username, String email) {
    return User.builder()
        .username(username)
        .password("{noop}password") // без шифрования для тестов
        .email(email)
        .streamKey(UUID.randomUUID())
        .enabled(true)
        .authorities(Set.of("ROLE_USER"))
        .avatarUrl("https://picsum.photos/200/300")
        .bio("Bio of " + username)
        .followerCount(0)
        .build();
  }
}
