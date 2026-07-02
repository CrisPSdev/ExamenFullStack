package cl.duoc.itinerary.dto.request;

import cl.duoc.itinerary.enums.ItineraryItemType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ItineraryItemCreateRequestDTO {

    @NotNull(message = "El tipo de item es obligatorio")
    private ItineraryItemType itemType;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDate scheduledDate;

    private LocalTime scheduledTime;

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    private String notes;

    @Min(value = 0, message = "El orden debe ser mayor o igual a 0")
    private int orderIndex;
}
