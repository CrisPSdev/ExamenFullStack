-- Itinerario de ejemplo para viaje a Santiago
INSERT INTO itineraries (id, trip_id, title, description, start_date, end_date, created_at)
VALUES (
    UUID_TO_BIN(UUID()),
    UUID_TO_BIN('11111111-1111-1111-1111-111111111111'),
    'Itinerario Santiago',
    'Plan de 3 dias para conocer Santiago',
    '2026-08-01',
    '2026-08-03',
    NOW()
);

-- Items del itinerario de ejemplo
INSERT INTO itinerary_items (id, itinerary_id, item_type, name, scheduled_date, scheduled_time, notes, order_index)
VALUES (
    UUID_TO_BIN(UUID()),
    (SELECT id FROM itineraries WHERE title = 'Itinerario Santiago'),
    'HOTEL',
    'Hotel Plaza',
    '2026-08-01',
    '15:00:00',
    'Check-in al llegar',
    0
);

INSERT INTO itinerary_items (id, itinerary_id, item_type, name, scheduled_date, scheduled_time, notes, order_index)
VALUES (
    UUID_TO_BIN(UUID()),
    (SELECT id FROM itineraries WHERE title = 'Itinerario Santiago'),
    'TOUR',
    'Tour Vina Concha y Toro',
    '2026-08-02',
    '10:00:00',
    'Reservar con anticipacion',
    1
);

INSERT INTO itinerary_items (id, itinerary_id, item_type, name, scheduled_date, scheduled_time, notes, order_index)
VALUES (
    UUID_TO_BIN(UUID()),
    (SELECT id FROM itineraries WHERE title = 'Itinerario Santiago'),
    'SITIO_TURISTICO',
    'Cerro San Cristobal',
    '2026-08-03',
    '09:00:00',
    'Llevar agua y protector solar',
    2
);
