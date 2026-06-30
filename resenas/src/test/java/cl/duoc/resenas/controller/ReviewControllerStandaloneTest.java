package cl.duoc.resenas.controller;

import cl.duoc.resenas.dto.ApiResponse;
import cl.duoc.resenas.dto.RatingItemDTO;
import cl.duoc.resenas.dto.ReviewRequestDTO;
import cl.duoc.resenas.dto.ReviewResponseDTO;
import cl.duoc.resenas.exception.GlobalExceptionHandler;
import cl.duoc.resenas.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewControllerStandaloneTest {

    private ReviewService reviewService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        reviewService = Mockito.mock(ReviewService.class);
        ReviewController controller = new ReviewController(reviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private ReviewRequestDTO buildRequest() {
        ReviewRequestDTO dto = new ReviewRequestDTO();
        dto.setDestinationId(UUID.randomUUID());
        dto.setRating(4);
        dto.setTitle("Hotel Plaza");
        dto.setComment("Muy buena atención");
        dto.setRatings(List.of(new RatingItemDTO("Servicio", 5)));
        return dto;
    }

    private ReviewResponseDTO buildResponse(UUID id) {
        return new ReviewResponseDTO(
                id, UUID.randomUUID(), UUID.randomUUID(),
                4, "Hotel Plaza", "Muy buena atención",
                LocalDateTime.now(), LocalDateTime.now(),
                List.of(new RatingItemDTO("Servicio", 5)));
    }

    @Test
    void createReview_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(reviewService.createReview(eq("token123"), any(ReviewRequestDTO.class)))
                .thenReturn(buildResponse(id));

        mockMvc.perform(post("/api/v1/resenas/reviews")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    void createReview_whenRatingOutOfRange_returns400() throws Exception {
        ReviewRequestDTO dto = buildRequest();
        dto.setRating(6);

        mockMvc.perform(post("/api/v1/resenas/reviews")
                        .header("Authorization", "Bearer token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getReviewById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(reviewService.getReviewById(id)).thenReturn(buildResponse(id));

        mockMvc.perform(get("/api/v1/resenas/reviews/{id}", id)
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void getReviewById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(reviewService.getReviewById(id))
                .thenThrow(new RuntimeException("Reseña no encontrada con id: " + id));

        mockMvc.perform(get("/api/v1/resenas/reviews/{id}", id)
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getReviewsByUser_returns200() throws Exception {
        Mockito.when(reviewService.getReviewsByUser("token123"))
                .thenReturn(List.of(buildResponse(UUID.randomUUID())));

        mockMvc.perform(get("/api/v1/resenas/reviews/user")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void deleteReview_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.doNothing().when(reviewService).deleteReview("token123", id);

        mockMvc.perform(delete("/api/v1/resenas/reviews/{id}", id)
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
