package cl.duoc.itinerary.controller;

import cl.duoc.itinerary.client.AuthClient;
import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.request.ItineraryItemCreateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryItemResponseDTO;
import cl.duoc.itinerary.dto.response.UserDTO;
import cl.duoc.itinerary.enums.ItineraryItemType;
import cl.duoc.itinerary.exception.GlobalExceptionHandler;
import cl.duoc.itinerary.service.ItineraryItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItineraryItemControllerStandaloneTest {

    private MockMvc mockMvc;
    private ItineraryItemService itemService;
    private AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ItineraryItemCreateRequestDTO buildRequest() {
        ItineraryItemCreateRequestDTO dto = new ItineraryItemCreateRequestDTO();
        dto.setItemType(ItineraryItemType.HOTEL);
        dto.setName("Hotel Plaza");
        dto.setScheduledDate(LocalDate.of(2026, 8, 1));
        dto.setScheduledTime(LocalTime.of(15, 0));
        dto.setNotes("Check-in");
        dto.setOrderIndex(0);
        return dto;
    }

    private ItineraryItemResponseDTO responseDTO() {
        return new ItineraryItemResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "HOTEL",
                "Hotel Plaza",
                LocalDate.of(2026, 8, 1),
                LocalTime.of(15, 0),
                "Check-in",
                0
        );
    }

    @BeforeEach
    void setup() {
        itemService = Mockito.mock(ItineraryItemService.class);
        authClient = Mockito.mock(AuthClient.class);
        ItineraryItemController controller = new ItineraryItemController(itemService, authClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserDTO user = new UserDTO();
        user.setId(UUID.randomUUID());
        user.setUsername("usuario");
        Mockito.when(authClient.validateToken(Mockito.anyString())).thenReturn(new ApiResponse<>(200, "Token valido", user));
    }

    @Test
    void addItem_returns200() throws Exception {
        UUID itineraryId = UUID.randomUUID();
        Mockito.when(itemService.addItem(Mockito.any(UUID.class), Mockito.any())).thenReturn(responseDTO());

        mockMvc.perform(post("/api/v1/itinerary/items/{itineraryId}", itineraryId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("Hotel Plaza"));
    }

    @Test
    void addItem_whenInvalidType_returns400() throws Exception {
        UUID itineraryId = UUID.randomUUID();
        ItineraryItemCreateRequestDTO dto = buildRequest();
        dto.setItemType(null);

        mockMvc.perform(post("/api/v1/itinerary/items/{itineraryId}", itineraryId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void deleteItem_returns200() throws Exception {
        UUID itemId = UUID.randomUUID();

        Mockito.doNothing().when(itemService).deleteItem(itemId);

        mockMvc.perform(delete("/api/v1/itinerary/items/{id}", itemId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
