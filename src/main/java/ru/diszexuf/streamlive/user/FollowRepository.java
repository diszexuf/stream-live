package ru.diszexuf.streamlive.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {
    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowing(User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    void deleteByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
} 