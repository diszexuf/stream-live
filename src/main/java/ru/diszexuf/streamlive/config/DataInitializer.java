//package ru.diszexuf.streamlive.config;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import ru.diszexuf.streamlive.stream.Stream;
//import ru.diszexuf.streamlive.stream.StreamRepository;
//import ru.diszexuf.streamlive.user.Follow;
//import ru.diszexuf.streamlive.user.User;
//import ru.diszexuf.streamlive.user.UserRepository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//public class DataInitializer implements CommandLineRunner {
//
//  private final UserRepository userRepository;
//  private final FollowRepository followRepository;
//  private final StreamRepository streamRepository;
//
//  @Override
//  @Transactional
//  public void run(String... args) {
//    User alice = createUser("alice", "alice@example.com");
//    User bob = createUser("bob", "bob@example.com");
//    User charlie = createUser("charlie", "charlie@example.com");
//    User diana = createUser("diana", "diana@example.com");
//    User eve = createUser("eve", "eve@example.com");
//
//    userRepository.saveAll(List.of(alice, bob, charlie, diana, eve));
//
//    followRepository.save(Follow.builder().follower(alice).following(bob).build());
//    followRepository.save(Follow.builder().follower(charlie).following(alice).build());
//    followRepository.save(Follow.builder().follower(diana).following(eve).build());
//    followRepository.save(Follow.builder().follower(eve).following(alice).build());
//
//    createAndSaveStream(alice, "Готовим вкусно", Set.of("кулинария", "еда", "дом"), true, LocalDateTime.now().minusMinutes(10), 42);
//    createAndSaveStream(alice, "Тихий вечер", Set.of(), false, LocalDateTime.now().minusHours(2), 3);
//
//    createAndSaveStream(bob, "Играем вместе", Set.of("игры", "онлайн"), true, LocalDateTime.now().minusMinutes(20), 10);
//    createAndSaveStream(bob, "Боб делится новостями", Set.of("жизнь", "блог"), false, LocalDateTime.now().minusDays(1), 5);
//    createAndSaveStream(bob, "Прямой эфир: Пишем код", Set.of("программирование", "разработка"), true, LocalDateTime.now().minusMinutes(30), 15);
//
//    createAndSaveStream(charlie, "Чарли о технологиях", Set.of("технологии", "интернет"), true, LocalDateTime.now().minusMinutes(5), 8);
//    createAndSaveStream(charlie, "Философские мысли", Set.of("размышления"), false, LocalDateTime.now().minusHours(3), 1);
//
//    createAndSaveStream(diana, "Арт-мастерская Дианы", Set.of("живопись", "рисование"), true, LocalDateTime.now().minusMinutes(15), 7);
//    createAndSaveStream(diana, "Вечерние зарисовки", Set.of("эскизы", "расслабление"), false, LocalDateTime.now().minusHours(5), 2);
//    createAndSaveStream(diana, "Урок живописи онлайн", Set.of("урок", "акварель"), true, LocalDateTime.now().minusMinutes(45), 9);
//
//    createAndSaveStream(eve, "Музыка в прямом эфире", Set.of("пение", "живое выступление"), true, LocalDateTime.now().minusMinutes(8), 12);
//    createAndSaveStream(eve, "Теория музыки", Set.of("теория", "музыкальная грамота"), false, LocalDateTime.now().minusHours(4), 6);
//    createAndSaveStream(eve, "Джем-сейшн в прямом эфиру", Set.of("джаз", "совместное исполнение"), true, LocalDateTime.now().minusMinutes(12), 14);
//  }
//
//  private User createUser(String username, String email) {
//    UUID streamKey = UUID.randomUUID();
//
//    return User.builder()
//        .username(username)
//        .password("{noop}password")
//        .email(email)
//        .streamKey(streamKey)
//        .enabled(true)
//        .authorities(Set.of("ROLE_USER"))
//        .avatarUrl("https://picsum.photos/200/300?random=" + System.currentTimeMillis())
//        .bio("Описание пользователя " + username)
//        .followerCount(0)
//        .build();
//  }
//
//  private void createAndSaveStream(User user, String title, Set<String> tags,
//                                   boolean isLive, LocalDateTime startedAt, int viewersCount) {
//    streamRepository.save(Stream.builder()
//        .user(user)
//        .title(title)
//        .description(title + "... подробнее в прямом эфире!")
//        .thumbnailUrl("https://picsum.photos/200/300?random=" + System.currentTimeMillis())
//        .tags(tags)
//        .isLive(isLive)
//        .startedAt(startedAt)
//        .endedAt(isLive ? null : startedAt.plusHours(2))
//        .viewersCount(viewersCount)
//        .build());
//  }
//}