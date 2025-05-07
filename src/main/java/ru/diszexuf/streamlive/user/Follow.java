package ru.diszexuf.streamlive.user;

import jakarta.persistence.*;
import lombok.*;
import ru.diszexuf.streamlive.common.CoreEntity;

@Entity
@Table(name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_follow_pair", columnNames = {"follower_id", "following_id"})
        },
        indexes = {
                @Index(name = "idx_follow_follower", columnList = "follower_id"),
                @Index(name = "idx_follow_following", columnList = "following_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follow_follower"))
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false, foreignKey = @ForeignKey(name = "fk_follow_following"))
    private User following;

}