package ru.diszexuf.streamlive.user;

import jakarta.persistence.*;
import lombok.*;
import ru.diszexuf.streamlive.common.CoreEntity;

import java.util.UUID;

@Entity
@Table(name = "follows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_follow_follower"))
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", unique = true, nullable = false, foreignKey = @ForeignKey(name = "fk_follow_following"))
    private User following;

}