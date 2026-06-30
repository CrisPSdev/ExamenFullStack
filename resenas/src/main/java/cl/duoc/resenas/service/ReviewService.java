package cl.duoc.resenas.service;

import cl.duoc.resenas.dto.*;
import cl.duoc.resenas.exception.BusinessRuleException;
import cl.duoc.resenas.exception.DuplicateResourceException;
import cl.duoc.resenas.exception.ResourceNotFoundException;
import cl.duoc.resenas.exception.UnauthorizedException;
import cl.duoc.resenas.model.Review;
import cl.duoc.resenas.model.ReviewRating;
import cl.duoc.resenas.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final AuthService authService;
    private final DestinationService destinationService;

    @Transactional
    public ReviewResponseDTO createReview(String token, ReviewRequestDTO dto) {
        UUID userId = requireValidUser(token);

        ApiResponse<Boolean> destResponse = destinationService.validateDestination(dto.getDestinationId(), token);
        if (destResponse == null || destResponse.getCode() != 200 || destResponse.getData() == null || !destResponse.getData()) {
            throw new BusinessRuleException("Destino inválido o inexistente");
        }

        if (reviewRepository.existsByUserIdAndDestinationId(userId, dto.getDestinationId())) {
            throw new DuplicateResourceException("Ya existe una reseña del usuario para este destino");
        }

        Review review = new Review();
        review.setUserId(userId);
        review.setDestinationId(dto.getDestinationId());
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());
        attachRatings(review, dto.getRatings());

        Review saved = reviewRepository.save(review);
        logger.info("Reseña creada id={} para destino={} por usuario={}", saved.getId(), saved.getDestinationId(), userId);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));
        return toResponseDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByDestination(UUID destinationId, String token) {
        ApiResponse<Boolean> destResponse = destinationService.validateDestination(destinationId, token);
        if (destResponse == null || destResponse.getCode() != 200 || destResponse.getData() == null || !destResponse.getData()) {
            throw new BusinessRuleException("Destino inválido o inexistente");
        }
        return reviewRepository.findByDestinationId(destinationId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getReviewsByUser(String token) {
        UUID userId = requireValidUser(token);
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDTO updateReview(String token, UUID id, ReviewRequestDTO dto) {
        UUID userId = requireValidUser(token);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("No tiene permiso para modificar esta reseña");
        }

        if (dto.getDestinationId() != null && !dto.getDestinationId().equals(review.getDestinationId())) {
            ApiResponse<Boolean> destResponse = destinationService.validateDestination(dto.getDestinationId(), token);
            if (destResponse == null || destResponse.getCode() != 200 || destResponse.getData() == null || !destResponse.getData()) {
                throw new BusinessRuleException("Destino inválido o inexistente");
            }
            review.setDestinationId(dto.getDestinationId());
        }

        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());

        review.getRatings().clear();
        attachRatings(review, dto.getRatings());

        Review updated = reviewRepository.save(review);
        logger.info("Reseña actualizada id={} por usuario={}", updated.getId(), userId);
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteReview(String token, UUID id) {
        UUID userId = requireValidUser(token);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada con id: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new UnauthorizedException("No tiene permiso para eliminar esta reseña");
        }

        reviewRepository.delete(review);
        logger.info("Reseña eliminada id={} por usuario={}", id, userId);
    }

    private UUID requireValidUser(String token) {
        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse == null || authResponse.getCode() != 200 || authResponse.getData() == null) {
            throw new UnauthorizedException("Token inválido");
        }
        return authResponse.getData().getId();
    }

    private void attachRatings(Review review, List<RatingItemDTO> ratings) {
        if (ratings == null) return;
        for (RatingItemDTO item : ratings) {
            ReviewRating rating = new ReviewRating();
            rating.setReview(review);
            rating.setCategory(item.getCategory());
            rating.setScore(item.getScore());
            review.getRatings().add(rating);
        }
    }

    private ReviewResponseDTO toResponseDTO(Review review) {
        List<RatingItemDTO> ratingDTOs = review.getRatings() == null ? new ArrayList<>()
                : review.getRatings().stream()
                        .map(r -> new RatingItemDTO(r.getCategory(), r.getScore()))
                        .collect(Collectors.toList());
        return new ReviewResponseDTO(
                review.getId(),
                review.getUserId(),
                review.getDestinationId(),
                review.getRating(),
                review.getTitle(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                ratingDTOs
        );
    }
}
