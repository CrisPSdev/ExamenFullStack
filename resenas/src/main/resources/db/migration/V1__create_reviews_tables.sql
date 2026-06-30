CREATE TABLE reviews (
    id              BINARY(16)   NOT NULL,
    user_id         BINARY(16)   NOT NULL,
    destination_id  BINARY(16)   NOT NULL,
    rating          INT          NOT NULL,
    title           VARCHAR(100),
    comment         VARCHAR(1000),
    created_at      DATETIME     NOT NULL,
    updated_at      DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_destination (user_id, destination_id)
);

CREATE TABLE review_ratings (
    id          BINARY(16)  NOT NULL,
    review_id   BINARY(16)  NOT NULL,
    category    VARCHAR(50) NOT NULL,
    score       INT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_review FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);
