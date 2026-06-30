package cl.duoc.resenas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReviewResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID destinationId;
    private Integer rating;
    private String title;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RatingItemDTO> ratings;
}
