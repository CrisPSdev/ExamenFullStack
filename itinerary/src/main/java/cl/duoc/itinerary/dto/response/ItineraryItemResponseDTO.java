package cl.duoc.itinerary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryItemResponseDTO {

    private UUID id;
    private UUID itineraryId;
    private String itemType;
    private String name;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private String notes;
    private int orderIndex;
}
