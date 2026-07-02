package cl.duoc.itinerary.service;

import cl.duoc.itinerary.client.TravelerClient;
import cl.duoc.itinerary.dto.request.ItineraryCreateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryDetailResponseDTO;
import cl.duoc.itinerary.dto.response.ItineraryResponseDTO;
import cl.duoc.itinerary.model.Itinerary;
import cl.duoc.itinerary.model.ItineraryItem;
import cl.duoc.itinerary.enums.ItineraryItemType;
import cl.duoc.itinerary.repository.ItineraryRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItineraryServiceTest {

    private ItineraryCreateRequestDTO buildRequest(UUID tripId) {
        ItineraryCreateRequestDTO dto = new ItineraryCreateRequestDTO();
        dto.setTripId(tripId);
        dto.setTitle("Itinerario Santiago");
        dto.setDescription("Plan de viaje");
        dto.setStartDate(LocalDate.of(2026, 8, 1));
        dto.setEndDate(LocalDate.of(2026, 8, 3));
        return dto;
    }

    private Itinerary savedEntity(UUID id) {
        Itinerary itinerary = new Itinerary();
        itinerary.setId(id);
        itinerary.setTripId(UUID.randomUUID());
        itinerary.setTitle("Itinerario Santiago");
        itinerary.setDescription("Plan de viaje");
        itinerary.setStartDate(LocalDate.of(2026, 8, 1));
        itinerary.setEndDate(LocalDate.of(2026, 8, 3));
        itinerary.setCreatedAt(LocalDateTime.now());
        return itinerary;
    }

    @Test
    void createItinerary_whenTripExists_savesAndReturnsDTO() {
        ItineraryRepository repository = Mockito.mock(ItineraryRepository.class);
        TravelerClient travelerClient = Mockito.mock(TravelerClient.class);
        ItineraryService service = new ItineraryService(repository, travelerClient);

        UUID tripId = UUID.randomUUID();
        UUID itineraryId = UUID.randomUUID();

        Mockito.when(travelerClient.tripExists(tripId, "token")).thenReturn(true);
        Mockito.when(repository.save(Mockito.any(Itinerary.class))).thenReturn(savedEntity(itineraryId));

        ItineraryResponseDTO result = service.createItinerary(buildRequest(tripId), "token");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itineraryId);
        Mockito.verify(repository).save(Mockito.any(Itinerary.class));
    }

    @Test
    void createItinerary_whenTripDoesNotExist_throwsRuntimeException() {
        ItineraryRepository repository = Mockito.mock(ItineraryRepository.class);
        TravelerClient travelerClient = Mockito.mock(TravelerClient.class);
        ItineraryService service = new ItineraryService(repository, travelerClient);

        UUID tripId = UUID.randomUUID();

        Mockito.when(travelerClient.tripExists(tripId, "token")).thenReturn(false);

        assertThatThrownBy(() -> service.createItinerary(buildRequest(tripId), "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Viaje no encontrado");

        Mockito.verify(repository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void createItinerary_whenEndDateBeforeStartDate_throwsRuntimeException() {
        ItineraryRepository repository = Mockito.mock(ItineraryRepository.class);
        TravelerClient travelerClient = Mockito.mock(TravelerClient.class);
        ItineraryService service = new ItineraryService(repository, travelerClient);

        ItineraryCreateRequestDTO dto = buildRequest(UUID.randomUUID());
        dto.setStartDate(LocalDate.of(2026, 8, 5));
        dto.setEndDate(LocalDate.of(2026, 8, 1));

        assertThatThrownBy(() -> service.createItinerary(dto, "token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La fecha de termino no puede ser anterior");
    }

    @Test
    void getItineraryById_returnsItineraryWithItems() {
        ItineraryRepository repository = Mockito.mock(ItineraryRepository.class);
        TravelerClient travelerClient = Mockito.mock(TravelerClient.class);
        ItineraryService service = new ItineraryService(repository, travelerClient);

        UUID itineraryId = UUID.randomUUID();
        Itinerary itinerary = savedEntity(itineraryId);

        ItineraryItem item = new ItineraryItem();
        item.setId(UUID.randomUUID());
        item.setItinerary(itinerary);
        item.setItemType(ItineraryItemType.HOTEL);
        item.setName("Hotel Plaza");
        item.setScheduledDate(LocalDate.of(2026, 8, 1));
        item.setScheduledTime(LocalTime.of(15, 0));
        item.setNotes("Check-in");
        item.setOrderIndex(0);

        itinerary.getItems().add(item);

        Mockito.when(repository.findById(itineraryId)).thenReturn(Optional.of(itinerary));

        ItineraryDetailResponseDTO result = service.getItineraryById(itineraryId);

        assertThat(result).isNotNull();
        assertThat(result.getItinerary().getId()).isEqualTo(itineraryId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Hotel Plaza");
    }

    @Test
    void deleteItinerary_deletesItinerary() {
        ItineraryRepository repository = Mockito.mock(ItineraryRepository.class);
        TravelerClient travelerClient = Mockito.mock(TravelerClient.class);
        ItineraryService service = new ItineraryService(repository, travelerClient);

        UUID itineraryId = UUID.randomUUID();
        Mockito.when(repository.findById(itineraryId)).thenReturn(Optional.of(savedEntity(itineraryId)));

        service.deleteItinerary(itineraryId);

        Mockito.verify(repository).delete(Mockito.any(Itinerary.class));
    }
}
