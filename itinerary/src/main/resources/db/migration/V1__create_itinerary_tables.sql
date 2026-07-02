CREATE TABLE itineraries (
    id BINARY(16) NOT NULL,
    trip_id BINARY(16) NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE itinerary_items (
    id BINARY(16) NOT NULL,
    itinerary_id BINARY(16) NOT NULL,
    item_type VARCHAR(30) NOT NULL,
    name VARCHAR(100) NOT NULL,
    scheduled_date DATE NOT NULL,
    scheduled_time TIME,
    notes TEXT,
    order_index INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_itinerary_item_itinerary FOREIGN KEY (itinerary_id) REFERENCES itineraries(id)
);
