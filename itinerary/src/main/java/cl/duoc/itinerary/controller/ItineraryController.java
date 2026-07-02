package cl.duoc.itinerary.controller;

import cl.duoc.itinerary.client.AuthClient;
import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.request.ItineraryCreateRequestDTO;
import cl.duoc.itinerary.dto.request.ItineraryUpdateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryDetailResponseDTO;
import cl.duoc.itinerary.dto.response.ItineraryResponseDTO;
import cl.duoc.itinerary.dto.response.UserDTO;
import cl.duoc.itinerary.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Itinerary Controller", description = "Endpoints para gestion de itinerarios de viaje.")
@RestController
@RequestMapping("/api/v1/itinerary/itineraries")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final AuthClient authClient;

    public ItineraryController(ItineraryService itineraryService, AuthClient authClient) {
        this.itineraryService = itineraryService;
        this.authClient = authClient;
    }

    @PostMapping
    @Operation(summary = "Crear itinerario", description = "Permite registrar un nuevo itinerario asociado a un viaje existente.")
    public ResponseEntity<ApiResponse<ItineraryResponseDTO>> createItinerary(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ItineraryCreateRequestDTO request) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryResponseDTO response = itineraryService.createItinerary(request, token);
        return ResponseEntity.ok(new ApiResponse<>(200, "Itinerario creado correctamente", response));
    }

    @GetMapping
    @Operation(summary = "Listar itinerarios", description = "Permite obtener un listado de todos los itinerarios registrados.")
    public ResponseEntity<ApiResponse<List<ItineraryResponseDTO>>> getAllItineraries(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        List<ItineraryResponseDTO> response = itineraryService.getAllItineraries();
        return ResponseEntity.ok(new ApiResponse<>(200, "Listado de itinerarios", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener itinerario por ID", description = "Permite consultar un itinerario especifico incluyendo sus items.")
    public ResponseEntity<ApiResponse<ItineraryDetailResponseDTO>> getItineraryById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryDetailResponseDTO response = itineraryService.getItineraryById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Itinerario encontrado", response));
    }

    @GetMapping("/trip/{tripId}")
    @Operation(summary = "Listar itinerarios por viaje", description = "Permite obtener los itinerarios asociados a un viaje.")
    public ResponseEntity<ApiResponse<List<ItineraryResponseDTO>>> getItinerariesByTrip(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID tripId) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        List<ItineraryResponseDTO> response = itineraryService.getItinerariesByTripId(tripId);
        return ResponseEntity.ok(new ApiResponse<>(200, "Itinerarios del viaje", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar itinerario", description = "Permite modificar los datos de un itinerario existente.")
    public ResponseEntity<ApiResponse<ItineraryResponseDTO>> updateItinerary(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody ItineraryUpdateRequestDTO request) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryResponseDTO response = itineraryService.updateItinerary(id, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Itinerario actualizado correctamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar itinerario", description = "Permite eliminar un itinerario y todos sus items asociados.")
    public ResponseEntity<ApiResponse<Void>> deleteItinerary(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        itineraryService.deleteItinerary(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Itinerario eliminado correctamente", null));
    }
}
