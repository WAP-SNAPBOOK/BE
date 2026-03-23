ALTER TABLE shop_menu_tags
  ADD COLUMN shop_tag_id BIGINT NULL;

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT uq_shop_menu_tags_shop_tag UNIQUE (shop_menu_id, shop_tag_id);

CREATE INDEX idx_shop_menu_tags_shop_tag
  ON shop_menu_tags (shop_tag_id, shop_menu_id);

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_shop_tag FOREIGN KEY (shop_tag_id) REFERENCES shop_tags(id);
