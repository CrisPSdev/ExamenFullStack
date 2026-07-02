package cl.duoc.itinerary.service;

import cl.duoc.itinerary.dto.request.ItineraryItemCreateRequestDTO;
import cl.duoc.itinerary.dto.request.ItineraryItemUpdateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryItemResponseDTO;
import cl.duoc.itinerary.enums.ItineraryItemType;
import cl.duoc.itinerary.model.Itinerary;
import cl.duoc.itinerary.model.ItineraryItem;
import cl.duoc.itinerary.repository.ItineraryItemRepository;
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

class ItineraryItemServiceTest {

    private Itinerary buildItinerary() {
        Itinerary itinerary = new Itinerary();
        itinerary.setId(UUID.randomUUID());
        itinerary.setTripId(UUID.randomUUID());
        itinerary.setTitle("Itinerario Santiago");
        itinerary.setStartDate(LocalDate.of(2026, 8, 1));
        itinerary.setEndDate(LocalDate.of(2026, 8, 3));
        itinerary.setCreatedAt(LocalDateTime.now());
        return itinerary;
    }

    private ItineraryItemCreateRequestDTO buildCreateRequest(LocalDate scheduledDate) {
        ItineraryItemCreateRequestDTO dto = new ItineraryItemCreateRequestDTO();
        dto.setItemType(ItineraryItemType.TOUR);
        dto.setName("Tour Vina Concha y Toro");
        dto.setScheduledDate(scheduledDate);
        dto.setScheduledTime(LocalTime.of(10, 0));
        dto.setNotes("Reservar con anticipacion");
        dto.setOrderIndex(1);
        return dto;
    }

    private ItineraryItemUpdateRequestDTO buildUpdateRequest(LocalDate scheduledDate) {
        ItineraryItemUpdateRequestDTO dto = new ItineraryItemUpdateRequestDTO();
        dto.setItemType(ItineraryItemType.SITIO_TURISTICO);
        dto.setName("Cerro San Cristobal");
        dto.setScheduledDate(scheduledDate);
        dto.setScheduledTime(LocalTime.of(9, 0));
        dto.setNotes("Llevar agua");
        dto.setOrderIndex(2);
        return dto;
    }

    @Test
    void addItem_whenDateInRange_savesAndReturnsDTO() {
        ItineraryItemRepository itemRepository = Mockito.mock(ItineraryItemRepository.class);
        ItineraryRepository itineraryRepository = Mockito.mock(ItineraryRepository.class);
        ItineraryItemService service = new ItineraryItemService(itemRepository, itineraryRepository);

        Itinerary itinerary = buildItinerary();
        UUID itemId = UUID.randomUUID();

        ItineraryItem savedItem = new ItineraryItem();
        savedItem.setId(itemId);
        savedItem.setItinerary(itinerary);
        savedItem.setItemType(ItineraryItemType.TOUR);
        savedItem.setName("Tour Vina Concha y Toro");
        savedItem.setScheduledDate(LocalDate.of(2026, 8, 2));
        savedItem.setOrderIndex(1);

        Mockito.when(itineraryRepository.findById(itinerary.getId())).thenReturn(Optional.of(itinerary));
        Mockito.when(itemRepository.save(Mockito.any(ItineraryItem.class))).thenReturn(savedItem);

        ItineraryItemResponseDTO result = service.addItem(itinerary.getId(), buildCreateRequest(LocalDate.of(2026, 8, 2)));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        Mockito.verify(itemRepository).save(Mockito.any(ItineraryItem.class));
    }

    @Test
    void addItem_whenDateOutOfRange_throwsRuntimeException() {
        ItineraryItemRepository itemRepository = Mockito.mock(ItineraryItemRepository.class);
        ItineraryRepository itineraryRepository = Mockito.mock(ItineraryRepository.class);
        ItineraryItemService service = new ItineraryItemService(itemRepository, itineraryRepository);

        Itinerary itinerary = buildItinerary();

        Mockito.when(itineraryRepository.findById(itinerary.getId())).thenReturn(Optional.of(itinerary));

        assertThatThrownBy(() -> service.addItem(itinerary.getId(), buildCreateRequest(LocalDate.of(2026, 8, 10))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La fecha programada debe estar dentro del rango");

        Mockito.verify(itemRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void updateItem_updatesAndReturnsDTO() {
        ItineraryItemRepository itemRepository = Mockito.mock(ItineraryItemRepository.class);
        ItineraryRepository itineraryRepository = Mockito.mock(ItineraryRepository.class);
        ItineraryItemService service = new ItineraryItemService(itemRepository, itineraryRepository);

        Itinerary itinerary = buildItinerary();
        UUID itemId = UUID.randomUUID();

        ItineraryItem existingItem = new ItineraryItem();
        existingItem.setId(itemId);
        existingItem.setItinerary(itinerary);
        existingItem.setItemType(ItineraryItemType.HOTEL);
        existingItem.setName("Hotel Plaza");
        existingItem.setScheduledDate(LocalDate.of(2026, 8, 1));
        existingItem.setOrderIndex(0);

        ItineraryItem updatedItem = new ItineraryItem();
        updatedItem.setId(itemId);
        updatedItem.setItinerary(itinerary);
        updatedItem.setItemType(ItineraryItemType.SITIO_TURISTICO);
        updatedItem.setName("Cerro San Cristobal");
        updatedItem.setScheduledDate(LocalDate.of(2026, 8, 2));
        updatedItem.setOrderIndex(2);

        Mockito.when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        Mockito.when(itemRepository.save(Mockito.any(ItineraryItem.class))).thenReturn(updatedItem);

        ItineraryItemResponseDTO result = service.updateItem(itemId, buildUpdateRequest(LocalDate.of(2026, 8, 2)));

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Cerro San Cristobal");
        assertThat(result.getItemType()).isEqualTo("SITIO_TURISTICO");
    }
}
