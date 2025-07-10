package ru.diszexuf.streamlive.stream;

import jakarta.persistence.*;
import lombok.*;
import ru.diszexuf.streamlive.common.CoreEntity;
import ru.diszexuf.streamlive.user.User;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "streams",
        schema = "public",
        uniqueConstraints = {
                @UniqueConstraint(name = "", columnNames = {""})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream extends CoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_stream_user"))
    private User user;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "thumbnail_url", nullable = false, length = 250)
    private String thumbnailUrl;

    @ElementCollection
    @CollectionTable(
        name = "stream_tags",
        joinColumns = @JoinColumn(name = "stream_id")
    )
    @Column(name = "tag")
    private Set<String> tags;

    @Column(name = "is_live", nullable = false)
    private Boolean isLive;

    @Column(name = "viewers_count", nullable = false)
    private Integer viewersCount;

    @OneToOne
    @JoinColumn(name = "stream_metadata_id")
    private StreamMetadata streamMetadata;

    @Enumerated(EnumType.STRING)
    private Privacy privacy;
}