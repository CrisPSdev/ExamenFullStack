package cl.duoc.resenas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "review_ratings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRating {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "score", nullable = false)
    private Integer score;

    @PrePersist
    public void generateId() {
        if (id == null) id = UUID.randomUUID();
    }
}
