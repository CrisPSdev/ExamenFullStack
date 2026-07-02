package cl.duoc.itinerary.controller;

import cl.duoc.itinerary.client.AuthClient;
import cl.duoc.itinerary.dto.ApiResponse;
import cl.duoc.itinerary.dto.request.ItineraryCreateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryResponseDTO;
import cl.duoc.itinerary.dto.response.UserDTO;
import cl.duoc.itinerary.exception.GlobalExceptionHandler;
import cl.duoc.itinerary.service.ItineraryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItineraryControllerStandaloneTest {

    private MockMvc mockMvc;
    private ItineraryService itineraryService;
    private AuthClient authClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private ItineraryCreateRequestDTO buildRequest() {
        ItineraryCreateRequestDTO dto = new ItineraryCreateRequestDTO();
        dto.setTripId(UUID.randomUUID());
        dto.setTitle("Itinerario Santiago");
        dto.setDescription("Plan de viaje");
        dto.setStartDate(LocalDate.of(2026, 8, 1));
        dto.setEndDate(LocalDate.of(2026, 8, 3));
        return dto;
    }

    private ItineraryResponseDTO responseDTO() {
        return new ItineraryResponseDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Itinerario Santiago",
                "Plan de viaje",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                LocalDateTime.now()
        );
    }

    @BeforeEach
    void setup() {
        itineraryService = Mockito.mock(ItineraryService.class);
        authClient = Mockito.mock(AuthClient.class);
        ItineraryController controller = new ItineraryController(itineraryService, authClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        UserDTO user = new UserDTO();
        user.setId(UUID.randomUUID());
        user.setUsername("usuario");
        Mockito.when(authClient.validateToken(Mockito.anyString())).thenReturn(new ApiResponse<>(200, "Token valido", user));
    }

    @Test
    void createItinerary_returns200() throws Exception {
        Mockito.when(itineraryService.createItinerary(Mockito.any(), Mockito.anyString())).thenReturn(responseDTO());

        mockMvc.perform(post("/api/v1/itinerary/itineraries")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("Itinerario Santiago"));
    }

    @Test
    void createItinerary_whenInvalidRequest_returns400() throws Exception {
        ItineraryCreateRequestDTO dto = buildRequest();
        dto.setTitle("");

        mockMvc.perform(post("/api/v1/itinerary/itineraries")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void getItineraryById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        ItineraryResponseDTO response = responseDTO();
        response.setId(id);

        Mockito.when(itineraryService.getItineraryById(id)).thenReturn(
                new cl.duoc.itinerary.dto.response.ItineraryDetailResponseDTO(response, java.util.Collections.emptyList())
        );

        mockMvc.perform(get("/api/v1/itinerary/itineraries/{id}", id)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.itinerary.title").value("Itinerario Santiago"));
    }

    @Test
    void createItinerary_whenTokenInvalid_returns401() throws Exception {
        Mockito.when(authClient.validateToken(Mockito.anyString())).thenReturn(new ApiResponse<>(401, "Token invalido", null));

        mockMvc.perform(post("/api/v1/itinerary/itineraries")
                        .header("Authorization", "Bearer invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }
}
