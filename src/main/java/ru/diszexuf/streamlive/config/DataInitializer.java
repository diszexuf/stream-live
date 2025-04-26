//package ru.diszexuf.streamlive.config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.instancio.Instancio;
//import org.instancio.Select;
//import org.springframework.context.annotation.Configuration;
//import ru.diszexuf.streamlive.model.Category;
//import ru.diszexuf.streamlive.model.Stream;
//import ru.diszexuf.streamlive.model.User;
//import ru.diszexuf.streamlive.repository.CategoryRepository;
//import ru.diszexuf.streamlive.repository.StreamRepository;
//import ru.diszexuf.streamlive.repository.UserRepository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Configuration
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer {
//
//    private final UserRepository userRepository;
//    private final CategoryRepository categoryRepository;
//    private final StreamRepository streamRepository;
//
//    @PostConstruct
//    public void init() {
//        log.info("Starting data initialization...");
//
//        if (userRepository.count() == 0) {
//            initUsers();
//        }
//
//        if (categoryRepository.count() == 0) {
//            initCategories();
//        }
//
//        if (streamRepository.count() == 0) {
//            initStreams();
//        }
//
//        log.info("Data initialization completed.");
//    }
//
//    private void initUsers() {
//        log.info("Initializing users...");
//
//        List<User> users = Instancio.ofList(User.class)
//                .size(3)
//                .supply(Select.field(User::getUsername), Instancio.generator().uuid())
//                .supply(Select.field(User::getEmail), Instancio.generator().internet().email())
//                .supply(Select.field(User::getPassword), "password")
//                .supply(Select.field(User::getAvatar), Instancio.generator().internet().imageUrl())
//                .supply(Select.field(User::getBio), Instancio.generator().lorem().sentence())
//                .supply(Select.field(User::getFollowersCount), Instancio.generator().integer(100, 1000))
//                .supply(Select.field(User::getCreatedAt), LocalDateTime.now())
//                .supply(Select.field(User::getUpdatedAt), LocalDateTime.now())
//                .create();
//
//        userRepository.saveAll(users);
//        log.info("Users initialized successfully.");
//    }
//
//    private void initCategories() {
//        log.info("Initializing categories...");
//
//        List<Category> categories = Instancio.ofList(Category.class)
//                .size(4)
//                .supply(Select.field(Category::getName), Instancio.generator().lorem().word())
//                .supply(Select.field(Category::getDescription), Instancio.generator().lorem().sentence())
//                .supply(Select.field(Category::getThumbnail), Instancio.generator().internet().imageUrl())
//                .create();
//
//        categoryRepository.saveAll(categories);
//        log.info("Categories initialized successfully.");
//    }
//
//    private void initStreams() {
//        log.info("Initializing streams...");
//
//        List<User> users = userRepository.findAll();
//        List<Category> categories = categoryRepository.findAll();
//
//        List<Stream> streams = Instancio.ofList(Stream.class)
//                .size(5)
//                .supply(Select.field(Stream::getTitle), Instancio.generator().lorem().sentence())
//                .supply(Select.field(Stream::getDescription), Instancio.generator().lorem().paragraph())
//                .supply(Select.field(Stream::getThumbnail), Instancio.generator().internet().imageUrl())
//                .supply(Select.field(Stream::getUser), () -> Instancio.random().from(users))
//                .supply(Select.field(Stream::getCategory), () -> Instancio.random().from(categories))
//                .supply(Select.field(Stream::getStreamKey), UUID::.randomUUID)
//                .supply(Select.field(Stream::getLive), Instancio.generator().booleanValue())
//                .supply(Select.field(Stream::getStartedAt), Instancio.generator().date().pastLocalDateTime())
//                .supply(Select.field(Stream::getCreatedAt), Instancio.generator().date().pastLocalDateTime())
//                .supply(Select.field(Stream::getUpdatedAt), Instancio.generator().date().pastLocalDateTime())
//                .create();
//
//        streamRepository.saveAll(streams);
//        log.info("Streams initialized successfully.");
//    }
//}