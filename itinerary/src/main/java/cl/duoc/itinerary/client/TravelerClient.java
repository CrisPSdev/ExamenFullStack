package cl.duoc.itinerary.client;

import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.response.TripResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TravelerClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Valida si un viaje existe en el Traveler Service.
     *
     * @param tripId ID del viaje a validar
     * @param token  JWT para autenticacion en el servicio remoto
     * @return true si el viaje existe, false en caso contrario
     */
    public boolean tripExists(UUID tripId, String token) {
        try {
            ApiResponse<TripResponseDTO> response = webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("traveler")
                            .path("/api/v1/traveler/trips/{id}")
                            .build(tripId))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<TripResponseDTO>>() {
                    })
                    .block();

            return response != null && response.getCode() == 200 && response.getData() != null;

        } catch (WebClientResponseException.NotFound ex) {
            return false;
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Error al consultar Traveler Service: " + ex.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo conectar con Traveler Service: " + ex.getMessage());
        }
    }
}
