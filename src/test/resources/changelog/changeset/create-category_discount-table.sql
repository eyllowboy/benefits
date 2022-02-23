CREATE TABLE IF NOT EXISTS category_discount (
  cd_id BIGSERIAL PRIMARY KEY,
  cd_category_id BIGINT,
  cd_discount_id BIGINT,
  FOREIGN KEY (cd_discount_id) REFERENCES discounts(id),
  FOREIGN KEY (cd_category_id) REFERENCES categories(id));

INSERT INTO category_discount (cd_category_id, cd_discount_id) VALUES
    (1, 1),
    (1, 2),
    (2, 3),
    (2, 4);
