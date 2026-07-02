package cl.duoc.itinerary.service;

import cl.duoc.itinerary.client.TravelerClient;
import cl.duoc.itinerary.dto.request.ItineraryCreateRequestDTO;
import cl.duoc.itinerary.dto.request.ItineraryUpdateRequestDTO;
import cl.duoc.itinerary.dto.response.ItineraryDetailResponseDTO;
import cl.duoc.itinerary.dto.response.ItineraryItemResponseDTO;
import cl.duoc.itinerary.dto.response.ItineraryResponseDTO;
import cl.duoc.itinerary.model.Itinerary;
import cl.duoc.itinerary.model.ItineraryItem;
import cl.duoc.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItineraryService {

    private final ItineraryRepository itineraryRepository;
    private final TravelerClient travelerClient;

    @Transactional
    public ItineraryResponseDTO createItinerary(ItineraryCreateRequestDTO dto, String token) {
        log.info("Creando itinerario para el viaje: {}", dto.getTripId());
        validateDateRange(dto.getStartDate(), dto.getEndDate());
        validateTripExists(dto.getTripId(), token);

        Itinerary itinerary = new Itinerary();
        itinerary.setTripId(dto.getTripId());
        itinerary.setTitle(dto.getTitle());
        itinerary.setDescription(dto.getDescription());
        itinerary.setStartDate(dto.getStartDate());
        itinerary.setEndDate(dto.getEndDate());

        Itinerary saved = itineraryRepository.save(itinerary);
        log.info("Itinerario creado con id: {}", saved.getId());
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ItineraryResponseDTO> getAllItineraries() {
        return itineraryRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItineraryDetailResponseDTO getItineraryById(UUID id) {
        log.debug("Buscando itinerario por id: {}", id);
        Itinerary itinerary = findItineraryById(id);

        List<ItineraryItemResponseDTO> items = itinerary.getItems()
                .stream()
                .map(this::toItemResponseDTO)
                .collect(Collectors.toList());

        return new ItineraryDetailResponseDTO(toResponseDTO(itinerary), items);
    }

    @Transactional(readOnly = true)
    public List<ItineraryResponseDTO> getItinerariesByTripId(UUID tripId) {
        return itineraryRepository.findByTripId(tripId)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ItineraryResponseDTO updateItinerary(UUID id, ItineraryUpdateRequestDTO dto) {
        log.info("Actualizando itinerario con id: {}", id);
        Itinerary itinerary = findItineraryById(id);
        validateDateRange(dto.getStartDate(), dto.getEndDate());

        itinerary.setTitle(dto.getTitle());
        itinerary.setDescription(dto.getDescription());
        itinerary.setStartDate(dto.getStartDate());
        itinerary.setEndDate(dto.getEndDate());

        Itinerary updated = itineraryRepository.save(itinerary);
        log.info("Itinerario actualizado con id: {}", updated.getId());
        return toResponseDTO(updated);
    }

    @Transactional
    public void deleteItinerary(UUID id) {
        log.info("Eliminando itinerario con id: {}", id);
        Itinerary itinerary = findItineraryById(id);
        itineraryRepository.delete(itinerary);
        log.info("Itinerario eliminado con id: {}", id);
    }

    private Itinerary findItineraryById(UUID id) {
        return itineraryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Itinerario no encontrado con id: {}", id);
                    return new RuntimeException("Itinerario no encontrado");
                });
    }

    private void validateTripExists(UUID tripId, String token) {
        if (!travelerClient.tripExists(tripId, token)) {
            log.warn("Viaje no encontrado con id: {}", tripId);
            throw new RuntimeException("Viaje no encontrado");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            log.warn("Rango de fechas invalido: inicio={}, termino={}", startDate, endDate);
            throw new RuntimeException("La fecha de termino no puede ser anterior a la fecha de inicio");
        }
    }

    private ItineraryResponseDTO toResponseDTO(Itinerary itinerary) {
        return new ItineraryResponseDTO(
                itinerary.getId(),
                itinerary.getTripId(),
                itinerary.getTitle(),
                itinerary.getDescription(),
                itinerary.getStartDate(),
                itinerary.getEndDate(),
                itinerary.getCreatedAt()
        );
    }

    private ItineraryItemResponseDTO toItemResponseDTO(ItineraryItem item) {
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
