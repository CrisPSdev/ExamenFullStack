package cl.duoc.resenas.service;

import cl.duoc.resenas.dto.ApiResponse;
import cl.duoc.resenas.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final WebClient.Builder webClientBuilder;

    /**
     * Valida el token contra el Login Service y retorna los datos del usuario.
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
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
                    .block();
        } catch (Exception e) {
            logger.error("Error al validar token contra Login Service: {}", e.getMessage());
            return new ApiResponse<>(500, "Error al validar token: " + e.getMessage(), null);
        }
    }
}
