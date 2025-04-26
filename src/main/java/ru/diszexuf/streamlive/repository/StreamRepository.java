package ru.diszexuf.streamlive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.diszexuf.streamlive.model.Category;
import ru.diszexuf.streamlive.model.Stream;
import ru.diszexuf.streamlive.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {
    List<Stream> findByUser(User user);
    List<Stream> findByCategory(Category category);
    List<Stream> findByIsLiveTrue();
    Optional<Stream> findByStreamKey(String streamKey);
} 