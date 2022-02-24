CREATE TABLE IF NOT EXISTS location_discount (
  id BIGSERIAL PRIMARY KEY,
  location_id BIGINT,
  discount_id BIGINT,
  FOREIGN KEY (discount_id) REFERENCES discounts(id),
  FOREIGN KEY (location_id) REFERENCES locations(id));

INSERT INTO location_discount (location_id, discount_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (2, 4);
