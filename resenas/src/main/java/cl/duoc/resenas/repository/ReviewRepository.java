package cl.duoc.resenas.repository;

import cl.duoc.resenas.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByDestinationId(UUID destinationId);

    List<Review> findByUserId(UUID userId);

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndDestinationId(UUID userId, UUID destinationId);
}
