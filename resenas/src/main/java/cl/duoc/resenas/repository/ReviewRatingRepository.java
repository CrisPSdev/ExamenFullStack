package cl.duoc.resenas.repository;

import cl.duoc.resenas.model.ReviewRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewRatingRepository extends JpaRepository<ReviewRating, UUID> {
}
