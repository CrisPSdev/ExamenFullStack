package cl.duoc.itinerary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryDetailResponseDTO {

    private ItineraryResponseDTO itinerary;
    private List<ItineraryItemResponseDTO> items;
}
