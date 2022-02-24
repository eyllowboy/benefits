CREATE TABLE IF NOT EXISTS category_discount (
  id BIGSERIAL PRIMARY KEY,
  category_id BIGINT,
  discount_id BIGINT,
  FOREIGN KEY (discount_id) REFERENCES discounts(id),
  FOREIGN KEY (category_id) REFERENCES categories(id));

INSERT INTO category_discount (category_id, discount_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (2, 4);
