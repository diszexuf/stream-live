package ru.diszexuf.streamlive.user;

import jakarta.persistence.*;
import lombok.*;
import ru.diszexuf.streamlive.common.CoreEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_username", columnList = "username"),
                @Index(name = "idx_user_email", columnList = "email")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> authorities = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "stream_key", unique = true, nullable = false)
    private UUID streamKey;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "follower_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer followerCount;

}