ALTER TABLE shop_menu_tags
  ADD COLUMN shop_tag_id BIGINT;

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT uq_shop_menu_tags_shop_tag UNIQUE (shop_menu_id, shop_tag_id);

CREATE INDEX idx_shop_menu_tags_shop_tag
  ON shop_menu_tags (shop_tag_id, shop_menu_id);
