package ru.diszexuf.streamlive.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.diszexuf.streamlive.model.Stream;
import ru.diszexuf.streamlive.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamRepository extends JpaRepository<Stream, UUID> {
    List<Stream> findByUser(User user);
    List<Stream> findByIsLiveTrue();
    Optional<Stream> findByStreamKey(String streamKey);
    
    @Query("SELECT s FROM Stream s WHERE LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Stream> findByTitleContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT DISTINCT s FROM Stream s JOIN s.tags t WHERE LOWER(t) = LOWER(:tag)")
    List<Stream> findByTag(@Param("tag") String tag);
    
    @Query("SELECT DISTINCT s FROM Stream s JOIN s.tags t WHERE LOWER(t) IN :tags")
    List<Stream> findByTagsIn(@Param("tags") List<String> tags);
    
    @Query("SELECT DISTINCT t FROM Stream s JOIN s.tags t GROUP BY t ORDER BY COUNT(s) DESC")
    List<String> findPopularTags();
} 