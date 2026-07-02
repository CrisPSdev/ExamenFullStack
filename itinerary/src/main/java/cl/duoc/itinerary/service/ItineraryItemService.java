package cl.duoc.itinerary.service;

import cl.duoc.itinerary.dto.request.ItineraryItemCreateRequestDTO;
import cl.duoc.itinerary.dto.request.ItineraryItemUpdateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryItemResponseDTO;
import cl.duoc.itinerary.model.Itinerary;
import cl.duoc.itinerary.model.ItineraryItem;
import cl.duoc.itinerary.repository.ItineraryItemRepository;
import cl.duoc.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryItemService {

    private final ItineraryItemRepository itemRepository;
    private final ItineraryRepository itineraryRepository;

    @Transactional
    public ItineraryItemResponseDTO addItem(UUID itineraryId, ItineraryItemCreateRequestDTO dto) {
        log.info("Agregando item al itinerario: {}", itineraryId);
        Itinerary itinerary = findItineraryById(itineraryId);
        validateItemDate(dto.getScheduledDate(), itinerary);

        ItineraryItem item = new ItineraryItem();
        item.setItinerary(itinerary);
        item.setItemType(dto.getItemType());
        item.setName(dto.getName());
        item.setScheduledDate(dto.getScheduledDate());
        item.setScheduledTime(dto.getScheduledTime());
        item.setNotes(dto.getNotes());
        item.setOrderIndex(dto.getOrderIndex());

        ItineraryItem saved = itemRepository.save(item);
        log.info("Item agregado con id: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public ItineraryItemResponseDTO getItemById(UUID id) {
        ItineraryItem item = findItemById(id);
        return toResponseDTO(item);
    }

    @Transactional
    public ItineraryItemResponseDTO updateItem(UUID id, ItineraryItemUpdateRequestDTO dto) {
        log.info("Actualizando item con id: {}", id);
        ItineraryItem item = findItemById(id);
        validateItemDate(dto.getScheduledDate(), item.getItinerary());

        item.setItemType(dto.getItemType());
        item.setName(dto.getName());
        item.setScheduledDate(dto.getScheduledDate());
        item.setScheduledTime(dto.getScheduledTime());
        item.setNotes(dto.getNotes());
        item.setOrderIndex(dto.getOrderIndex());

        ItineraryItem updated = itemRepository.save(item);
        log.info("Item actualizado con id: {}", updated.getId());
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteItem(UUID id) {
        log.info("Eliminando item con id: {}", id);
        ItineraryItem item = findItemById(id);
        itemRepository.delete(item);
        log.info("Item eliminado con id: {}", id);
    }

    private Itinerary findItineraryById(UUID id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Itinerario no encontrado con id: {}", id);
                    return new RuntimeException("Itinerario no encontrado");
                });
    }

    private ItineraryItem findItemById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Item del itinerario no encontrado con id: {}", id);
                    return new RuntimeException("Item del itinerario no encontrado");
                });
    }

    private void validateItemDate(java.time.LocalDate scheduledDate, Itinerary itinerary) {
        if (scheduledDate.isBefore(itinerary.getStartDate()) || scheduledDate.isAfter(itinerary.getEndDate())) {
            log.warn("Fecha programada fuera de rango: fecha={}, inicio={}, termino={}",
                    scheduledDate, itinerary.getStartDate(), itinerary.getEndDate());
            throw new RuntimeException("La fecha programada debe estar dentro del rango del itinerario");
        }
    }

    private ItineraryItemResponseDTO toResponseDTO(ItineraryItem item) {
        return new ItineraryItemResponseDTO(
                item.getId(),
                item.getItinerary().getId(),
                item.getItemType().name(),
                item.getName(),
                item.getScheduledDate(),
                item.getScheduledTime(),
                item.getNotes(),
                item.getOrderIndex()
        );
    }
}
