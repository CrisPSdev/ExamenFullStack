package cl.duoc.resenas.service;

import cl.duoc.resenas.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DestinationService {

    private static final Logger logger = LoggerFactory.getLogger(DestinationService.class);

    private final WebClient.Builder webClientBuilder;

    /**
     * Valida que un destino exista en el Destination Service.
     */
    public ApiResponse<Boolean> validateDestination(UUID destinationId, String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                        .scheme("http")
                        .host("destination")
                        .path("/api/v1/destination/destinations/exists")
                        .queryParam("id", destinationId)
                        .build())
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Boolean>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error al validar destino contra Destination Service: {}", e.getMessage());
            return new ApiResponse<>(500, "Error al validar destino: " + e.getMessage(), null);
        }
    }
}
