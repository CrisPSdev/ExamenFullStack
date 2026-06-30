package cl.duoc.resenas.service;

import cl.duoc.resenas.dto.ApiResponse;
import cl.duoc.resenas.dto.RatingItemDTO;
import cl.duoc.resenas.dto.ReviewRequestDTO;
import cl.duoc.resenas.dto.ReviewResponseDTO;
import cl.duoc.resenas.dto.UserDTO;
import cl.duoc.resenas.model.Review;
import cl.duoc.resenas.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewServiceTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID DEST_ID = UUID.randomUUID();
    private static final String TOKEN = "token123";

    private ReviewRequestDTO buildRequest() {
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setDestinationId(DEST_ID);
        dto.setRating(4);
        dto.setTitle("Hotel Plaza");
        dto.setComment("Muy buena atención");
        dto.setRatings(List.of(new RatingItemDTO("Servicio", 5)));
        return dto;
    }

    private Review savedReview() {
        Review r = new Review();
        r.setId(UUID.randomUUID());
        r.setUserId(USER_ID);
        r.setDestinationId(DEST_ID);
        r.setRating(4);
        r.setTitle("Hotel Plaza");
        r.setComment("Muy buena atención");
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return r;
    }

    private UserDTO userDTO() {
        UserDTO u = new UserDTO();
        u.setId(USER_ID);
        u.setUsername("juanperez");
        return u;
    }

    private ReviewService newService(ReviewRepository repo, AuthService auth, DestinationService dest) {
        return new ReviewService(repo, auth, dest);
    }

    @Test
    void createReview_whenValid_savesAndReturnsDTO() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", userDTO()));
        Mockito.when(dest.validateDestination(DEST_ID, TOKEN)).thenReturn(new ApiResponse<>(200, "ok", true));
        Mockito.when(repo.existsByUserIdAndDestinationId(USER_ID, DEST_ID)).thenReturn(false);
        Mockito.when(repo.save(Mockito.any(Review.class))).thenReturn(savedReview());

        ReviewResponseDTO result = service.createReview(TOKEN, buildRequest());

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
        assertThat(result.getDestinationId()).isEqualTo(DEST_ID);
        Mockito.verify(repo).save(Mockito.any(Review.class));
    }

    @Test
    void createReview_whenTokenInvalid_throwsUnauthorized() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(401, "Token inválido", null));

        assertThatThrownBy(() -> service.createReview(TOKEN, buildRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");

        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createReview_whenDestinationInvalid_throwsBusinessRule() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", userDTO()));
        Mockito.when(dest.validateDestination(DEST_ID, TOKEN)).thenReturn(new ApiResponse<>(404, "no", false));

        assertThatThrownBy(() -> service.createReview(TOKEN, buildRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Destino inválido");

        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createReview_whenDuplicate_throwsDuplicateResource() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", userDTO()));
        Mockito.when(dest.validateDestination(DEST_ID, TOKEN)).thenReturn(new ApiResponse<>(200, "ok", true));
        Mockito.when(repo.existsByUserIdAndDestinationId(USER_ID, DEST_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createReview(TOKEN, buildRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe");

        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getReviewById_whenNotFound_throwsResourceNotFound() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        UUID id = UUID.randomUUID();
        Mockito.when(repo.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getReviewById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no encontrada");
    }

    @Test
    void getReviewsByUser_returnsListOfUserReviews() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", userDTO()));
        Mockito.when(repo.findByUserId(USER_ID)).thenReturn(List.of(savedReview()));

        List<ReviewResponseDTO> result = service.getReviewsByUser(TOKEN);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void updateReview_whenNotAuthor_throwsUnauthorized() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        UUID reviewId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        UserDTO other = new UserDTO();
        other.setId(otherUser);
        other.setUsername("otro");

        Review existing = savedReview();
        existing.setId(reviewId);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", other));
        Mockito.when(repo.findById(reviewId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.updateReview(TOKEN, reviewId, buildRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permiso");

        Mockito.verify(repo, Mockito.never()).save(Mockito.any());
    }

    @Test
    void deleteReview_whenNotAuthor_throwsUnauthorized() {
        ReviewRepository repo = Mockito.mock(ReviewRepository.class);
        AuthService auth = Mockito.mock(AuthService.class);
        DestinationService dest = Mockito.mock(DestinationService.class);
        ReviewService service = newService(repo, auth, dest);

        UUID reviewId = UUID.randomUUID();
        UUID otherUser = UUID.randomUUID();
        UserDTO other = new UserDTO();
        other.setId(otherUser);
        other.setUsername("otro");

        Review existing = savedReview();
        existing.setId(reviewId);

        Mockito.when(auth.validateToken(TOKEN)).thenReturn(new ApiResponse<>(200, "ok", other));
        Mockito.when(repo.findById(reviewId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.deleteReview(TOKEN, reviewId))
                .isInstanceOf(RuntimeException.class);

        Mockito.verify(repo, Mockito.never()).delete(Mockito.any());
    }
}
