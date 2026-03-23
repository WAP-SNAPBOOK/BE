CREATE TABLE shop_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_tags_shop_name (shop_id, name),
  UNIQUE KEY uq_shop_tags_shop_sort (shop_id, sort_order),
  INDEX idx_shop_tags_shop_sort (shop_id, sort_order)
);
