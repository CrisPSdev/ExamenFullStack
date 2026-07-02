package cl.duoc.itinerary.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ItineraryCreateRequestDTO {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(max = 100, message = "El titulo no puede superar los 100 caracteres")
    private String title;

    @Size(max = 500, message = "La descripcion no puede superar los 500 caracteres")
    private String description;

    @NotNull(message = "El id del viaje es obligatorio")
    private UUID tripId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;

    @NotNull(message = "La fecha de termino es obligatoria")
    private LocalDate endDate;
}
