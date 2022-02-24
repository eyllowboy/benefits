CREATE TABLE IF NOT EXISTS location_discount (
  ld_id BIGSERIAL PRIMARY KEY,
  ld_location_id BIGINT,
  ld_discount_id BIGINT,
  FOREIGN KEY (ld_discount_id) REFERENCES discounts(id),
  FOREIGN KEY (ld_location_id) REFERENCES locations(id));
