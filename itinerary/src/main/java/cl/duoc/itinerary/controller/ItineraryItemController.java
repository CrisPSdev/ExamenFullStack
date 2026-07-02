package cl.duoc.itinerary.controller;

import cl.duoc.itinerary.client.AuthClient;
import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.request.ItineraryItemCreateRequestDTO;
import cl.duoc.itinerary.dto.request.ItineraryItemUpdateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryItemResponseDTO;
import cl.duoc.itinerary.dto.response.UserDTO;
import cl.duoc.itinerary.service.ItineraryItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Itinerary Item Controller", description = "Endpoints para gestion de items dentro de un itinerario.")
@RestController
@RequestMapping("/api/v1/itinerary/items")
public class ItineraryItemController {

    private final ItineraryItemService itemService;
    private final AuthClient authClient;

    public ItineraryItemController(ItineraryItemService itemService, AuthClient authClient) {
        this.itemService = itemService;
        this.authClient = authClient;
    }

    @PostMapping("/{itineraryId}")
    @Operation(summary = "Agregar item a itinerario", description = "Permite registrar un nuevo item en un itinerario existente.")
    public ResponseEntity<ApiResponse<ItineraryItemResponseDTO>> addItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID itineraryId,
            @Valid @RequestBody ItineraryItemCreateRequestDTO request) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryItemResponseDTO response = itemService.addItem(itineraryId, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Item agregado correctamente", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener item por ID", description = "Permite consultar un item especifico de un itinerario.")
    public ResponseEntity<ApiResponse<ItineraryItemResponseDTO>> getItemById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryItemResponseDTO response = itemService.getItemById(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Item encontrado", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar item", description = "Permite modificar los datos de un item existente.")
    public ResponseEntity<ApiResponse<ItineraryItemResponseDTO>> updateItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody ItineraryItemUpdateRequestDTO request) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        ItineraryItemResponseDTO response = itemService.updateItem(id, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Item actualizado correctamente", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar item", description = "Permite eliminar un item de un itinerario.")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<UserDTO> validationResponse = authClient.validateToken(token);

        if (validationResponse == null || validationResponse.getCode() != 200 || validationResponse.getData() == null) {
            return ResponseEntity.status(401).body(new ApiResponse<>(401, "Token invalido", null));
        }

        itemService.deleteItem(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "Item eliminado correctamente", null));
    }
}
