package cl.duoc.itinerary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID destinationId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String travelType;
    private Integer companions;
    private String notes;
}
