package cl.duoc.resenas.controller;

import cl.duoc.resenas.dto.ApiResponse;
import cl.duoc.resenas.dto.ReviewRequestDTO;
import cl.duoc.resenas.dto.ReviewResponseDTO;
import cl.duoc.resenas.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Reviews Controller", description = "Endpoints para gestión de reseñas y notas de destinos turísticos.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resenas/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Crear reseña", description = "Registra una nueva reseña validando el token del usuario (Login Service) y la existencia del destino (Destination Service).")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview(
            @Parameter(description = "Token JWT del usuario autenticado", required = true)
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewRequestDTO dto) {

        String token = authHeader.replace("Bearer ", "");
        ReviewResponseDTO data = reviewService.createReview(token, dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Reseña creada correctamente", data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reseña por ID", description = "Consulta una reseña mediante su identificador único.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getReviewById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        ReviewResponseDTO data = reviewService.getReviewById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Reseña encontrada", data));
    }

    @GetMapping("/destination/{destinationId}")
    @Operation(summary = "Listar reseñas por destino", description = "Obtiene todas las reseñas asociadas a un destino, validando su existencia en el Destination Service.")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByDestination(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID destinationId) {

        String token = authHeader.replace("Bearer ", "");
        List<ReviewResponseDTO> data = reviewService.getReviewsByDestination(destinationId, token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Listado de reseñas del destino", data));
    }

    @GetMapping("/user")
    @Operation(summary = "Listar reseñas del usuario autenticado", description = "Obtiene todas las reseñas creadas por el usuario validado mediante su token.")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getReviewsByUser(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        List<ReviewResponseDTO> data = reviewService.getReviewsByUser(token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Listado de reseñas del usuario", data));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reseña", description = "Modifica una reseña existente. Solo el autor de la reseña puede actualizarla.")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequestDTO dto) {

        String token = authHeader.replace("Bearer ", "");
        ReviewResponseDTO data = reviewService.updateReview(token, id, dto);
        return ResponseEntity.ok(new ApiResponse<>(200, "Reseña actualizada correctamente", data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reseña", description = "Elimina una reseña existente. Solo el autor de la reseña puede eliminarla.")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        reviewService.deleteReview(token, id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Reseña eliminada correctamente", null));
    }
}
