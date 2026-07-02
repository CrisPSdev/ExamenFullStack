package cl.duoc.itinerary.client;

import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.response.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Valida el token JWT contra el login-service registrado en Eureka.
     *
     * @param token JWT recibido en el header Authorization
     * @return ApiResponse con el resultado de la validacion y datos del usuario
     */
    public ApiResponse<UserDTO> validateToken(String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("login")
                            .path("/api/v1/users/validate")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {
                    })
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al validar token: " + e.getMessage(), null);
        }
    }
}
