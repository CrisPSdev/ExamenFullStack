package cl.duoc.resenas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReviewRequestDTO {

    @NotNull(message = "El destino es obligatorio")
    private UUID destinationId;

    @NotNull(message = "La nota general es obligatoria")
    @Min(value = 1, message = "La nota mínima es 1")
    @Max(value = 5, message = "La nota máxima es 5")
    private Integer rating;

    @Size(max = 100, message = "El título debe tener máximo 100 caracteres")
    private String title;

    @Size(max = 1000, message = "El comentario debe tener máximo 1000 caracteres")
    private String comment;

    @Valid
    private List<RatingItemDTO> ratings;
}
