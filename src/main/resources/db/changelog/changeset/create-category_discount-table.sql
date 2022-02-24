CREATE TABLE IF NOT EXISTS category_discount (
  cd_id BIGSERIAL PRIMARY KEY,
  cd_category_id BIGINT,
  cd_discount_id BIGINT,
  FOREIGN KEY (cd_discount_id) REFERENCES discounts(id),
  FOREIGN KEY (cd_category_id) REFERENCES categories(id));
