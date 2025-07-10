package ru.diszexuf.streamlive.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import ru.diszexuf.streamlive.common.CoreEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = {"username"}),
                @UniqueConstraint(name = "uk_users_email", columnNames = {"email"}),
                @UniqueConstraint(name = "uk_users_stream_key", columnNames = {"stream_key"})
        },
        indexes = {
                @Index(name = "idx_users_follower_count", columnList = "follower_count")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Column(name = "avatar_url", length = 200)
    private String avatarUrl;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "stream_key", nullable = false)
    private UUID streamKey;

    @Column(name = "follower_count", nullable = false)
    private Integer followerCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "users_authorities", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority")
    private Set<Authority> authorities = new HashSet<>();

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @LastModifiedDate
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

}