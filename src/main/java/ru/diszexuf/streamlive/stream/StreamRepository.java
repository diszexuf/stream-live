package ru.diszexuf.streamlive.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.diszexuf.streamlive.user.User;

import java.util.List;
import java.util.UUID;

public interface StreamRepository extends JpaRepository<Stream, UUID> {
  List<Stream> findByUser(User user);

  Stream findByUserAndIsLiveTrue(User user);

  List<Stream> findByIsLiveTrue();

  List<Stream> findByTitleContainingIgnoreCase(String title);

  @Query("SELECT DISTINCT s FROM Stream s JOIN s.tags t WHERE LOWER(t) = LOWER(:tag)")
  List<Stream> findByTag(@Param("tag") String tag);

}