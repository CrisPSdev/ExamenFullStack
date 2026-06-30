package cl.duoc.resenas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingItemDTO {

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría debe tener máximo 50 caracteres")
    private String category;

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 5, message = "El puntaje máximo es 5")
    private Integer score;
}
