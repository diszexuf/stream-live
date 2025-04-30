package ru.diszexuf.streamlive.config;

import lombok.RequiredArgsConstructor;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.diszexuf.streamlive.old.model.Follow;
import ru.diszexuf.streamlive.old.model.Stream;
import ru.diszexuf.streamlive.user.User;
import ru.diszexuf.streamlive.old.repository.FollowRepository;
import ru.diszexuf.streamlive.old.repository.StreamRepository;
import ru.diszexuf.streamlive.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

  private final UserRepository userRepository;
  private final StreamRepository streamRepository;
  private final FollowRepository followRepository;


  @Override
  public void run(String... args) {
    if (userRepository.count() == 0) {
      generateUsers(10);
      generateStreams(5);
      generateFollows(3);
    }
  }

  private void generateUsers(int count) {
    List<User> users = IntStream.range(0, count)
        .mapToObj(i -> Instancio.of(User.class)
            .ignore(Select.field(User::getId))
            .supply(Select.field(User::getUsername), () -> "user_" + UUID.randomUUID().toString().substring(0, 8))
            .supply(Select.field(User::getEmail), () -> UUID.randomUUID().toString() + "@example.com")
            .supply(Select.field(User::getPasswordHash), () -> "{noop}password")
            .supply(Select.field(User::getBio), () -> Instancio.create(String.class, Instancio.gen().string().alphaNumeric().length(20, 80)))
            .supply(Select.field(User::getAvatarUrl), () -> "https://picsum.photos/200/300?random=" + System.currentTimeMillis())
            .supply(Select.field(User::getFollowerCount), () -> 0)
            .create())
        .toList();

    userRepository.saveAll(users);
    System.out.println("✅ Создано пользователей: " + count);
  }

  private void generateStreams(int count) {
    List<User> users = userRepository.findAll();
    if (users.isEmpty()) return;

    List<Stream> streams = IntStream.range(0, count)
        .mapToObj(i -> Instancio.of(Stream.class)
            .ignore(Select.field(Stream::getId))
            .supply(Select.field(Stream::getTitle), () -> Instancio.create(String.class, Instancio.gen().string().length(5, 30)))
            .supply(Select.field(Stream::getDescription), () -> Instancio.create(String.class, Instancio.gen().string().length(50, 200)))
            .supply(Select.field(Stream::getThumbnailUrl), () -> "https://picsum.photos/400/300?random=" + System.currentTimeMillis())
            .supply(Select.field(Stream::getStreamKey), UUID::randomUUID)
            .supply(Select.field(Stream::getTags), () -> Set.of(
                Instancio.create(String.class, Instancio.gen().string().length(5, 15)),
                Instancio.create(String.class, Instancio.gen().string().length(5, 15)),
                Instancio.create(String.class, Instancio.gen().string().length(5, 15))
            ))
            .supply(Select.field(Stream::getIsLive), () -> Instancio.create(Boolean.class, Instancio.gen().bool().probabilityTrue(60)))
            .supply(Select.field(Stream::getViewersCount), () -> Instancio.create(Integer.class, Instancio.gen().intRange(0, 1000)))
            .supply(Select.field(Stream::getUser), () -> Instancio.select(users))
            .supply(Select.field(Stream::getCreatedAt), () -> LocalDateTime.now().minusDays(Instancio.create(Integer.class, Instancio.gen().intRange(0, 30))))
            .supply(Select.field(Stream::getUpdatedAt), Stream::getCreatedAt)
            .supply(Select.field(Stream::getVersion), () -> 0L)
            .create())
        .toList();

    streamRepository.saveAll(streams);
    System.out.println("🎥 Создано стримов: " + count);
  }

  private void generateFollows(int followsPerUser) {
    List<User> users = userRepository.findAll();
    if (users.size() < 2) return;

    Set<Follow> followSet = new HashSet<>();

    for (User follower : users) {
      Set<User> followingSet = new HashSet<>();
      for (int i = 0; i < followsPerUser; i++) {
        User following = Instancio.select(users);

        if (following.equals(follower) || followingSet.contains(following)) continue;

        Follow follow = Instancio.of(Follow.class)
            .ignore(Select.field(Follow::getId))
            .supply(Select.field(Follow::getFollower), () -> follower)
            .supply(Select.field(Follow::getFollowing), () -> following)
            .supply(Select.field(Follow::getCreatedAt), () -> LocalDateTime.now())
            .create();

        followSet.add(follow);
        followingSet.add(following);
        following.setFollowerCount(following.getFollowerCount() + 1);
      }
    }

    followRepository.saveAll(followSet);
    userRepository.saveAll(users);
    System.out.println("🔔 Создано подписок: " + followSet.size());
  }
}
