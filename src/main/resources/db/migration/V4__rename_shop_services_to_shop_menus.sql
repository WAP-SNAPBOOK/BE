-- #99 shop_services 계열 → shop_menus 계열 리네이밍 + key 컬럼 제거
-- Created: 2026-02-10
-- 대상 테이블: shop_services, shop_service_tags, shop_service_input_fields, reservation_services
-- 추가: key 컬럼 제거, field_key_snapshot 컬럼 제거, UNIQUE(shop_menu_id, label) 추가
-- 전제: 위 테이블에 데이터 없음 (기존 테이블 데이터는 영향 없음)

-- ============================================================
-- 1) FK 제약 DROP (V3에서 추가한 것들)
-- ============================================================
ALTER TABLE reservation_services DROP FOREIGN KEY fk_res_services_reservation;
ALTER TABLE reservation_services DROP FOREIGN KEY fk_res_services_shop_service;
ALTER TABLE reservation_menu_input_values DROP FOREIGN KEY fk_rmiv_res_service;
ALTER TABLE reservation_menu_input_values DROP FOREIGN KEY fk_rmiv_input_field;
ALTER TABLE shop_service_tags DROP FOREIGN KEY fk_sst_shop_service;
ALTER TABLE shop_service_tags DROP FOREIGN KEY fk_sst_tag;

-- ============================================================
-- 2) UNIQUE 제약 DROP (V1 CREATE TABLE + V3 ALTER TABLE)
-- ============================================================
-- shop_services
ALTER TABLE shop_services DROP INDEX uq_shop_services_shop_name;

-- shop_service_tags
ALTER TABLE shop_service_tags DROP INDEX uq_shop_service_tags;

-- shop_service_input_fields
ALTER TABLE shop_service_input_fields DROP INDEX uq_shop_service_input_fields;

-- reservation_services (V3)
ALTER TABLE reservation_services DROP INDEX uq_reservation_services_res_shop_service;

-- reservation_menu_input_values (V3)
ALTER TABLE reservation_menu_input_values DROP INDEX uq_res_menu_input_values;

-- ============================================================
-- 3) INDEX DROP (V1 CREATE TABLE)
-- ============================================================
-- shop_services
ALTER TABLE shop_services DROP INDEX idx_shop_services_shop_active_sort;

-- shop_service_tags
ALTER TABLE shop_service_tags DROP INDEX idx_shop_service_tags_tag;

-- shop_service_input_fields
ALTER TABLE shop_service_input_fields DROP INDEX idx_shop_service_input_fields_service;

-- reservation_services
ALTER TABLE reservation_services DROP INDEX idx_reservation_services_reservation;
ALTER TABLE reservation_services DROP INDEX idx_reservation_services_shop_service;

-- reservation_menu_input_values
ALTER TABLE reservation_menu_input_values DROP INDEX idx_res_menu_input_values_res_service;

-- ============================================================
-- 4) RENAME TABLE (4개)
-- ============================================================
RENAME TABLE shop_services TO shop_menus;
RENAME TABLE shop_service_tags TO shop_menu_tags;
RENAME TABLE shop_service_input_fields TO shop_menu_input_fields;
RENAME TABLE reservation_services TO reservation_menu_items;

-- ============================================================
-- 5) RENAME COLUMN (8개)
-- ============================================================
-- shop_menu_tags
ALTER TABLE shop_menu_tags RENAME COLUMN shop_service_id TO shop_menu_id;

-- shop_menu_input_fields
ALTER TABLE shop_menu_input_fields RENAME COLUMN shop_service_id TO shop_menu_id;

-- reservation_menu_items (3개: FK + 스냅샷 3개)
ALTER TABLE reservation_menu_items RENAME COLUMN shop_service_id TO shop_menu_id;
ALTER TABLE reservation_menu_items RENAME COLUMN service_name_snapshot TO menu_name_snapshot;
ALTER TABLE reservation_menu_items RENAME COLUMN service_description_snapshot TO menu_description_snapshot;
ALTER TABLE reservation_menu_items RENAME COLUMN service_duration_snapshot_minutes TO menu_duration_snapshot_minutes;

-- reservation_menu_input_values (2개)
ALTER TABLE reservation_menu_input_values RENAME COLUMN reservation_service_id TO reservation_menu_item_id;
ALTER TABLE reservation_menu_input_values RENAME COLUMN shop_service_input_field_id TO shop_menu_input_field_id;

-- ============================================================
-- 6) key 컬럼 제거 (id(PK)로 식별, label만 사용)
-- ============================================================
ALTER TABLE shop_menu_input_fields DROP COLUMN `key`;

-- field_key_snapshot 제거 (label_snapshot + type_snapshot으로 충분)
ALTER TABLE reservation_menu_input_values DROP COLUMN field_key_snapshot;

-- ============================================================
-- 7) UNIQUE 제약 재생성 (새 이름)
-- ============================================================
ALTER TABLE shop_menus
  ADD UNIQUE KEY uq_shop_menus_shop_name (shop_id, name);

ALTER TABLE shop_menu_tags
  ADD UNIQUE KEY uq_shop_menu_tags (shop_menu_id, tag_id);

-- key 대신 label로 중복 방지
ALTER TABLE shop_menu_input_fields
  ADD UNIQUE KEY uq_shop_menu_input_fields (shop_menu_id, label);

ALTER TABLE reservation_menu_items
  ADD UNIQUE KEY uq_reservation_menu_items_res_shop_menu (reservation_id, shop_menu_id);

ALTER TABLE reservation_menu_input_values
  ADD UNIQUE KEY uq_res_menu_input_values (reservation_menu_item_id, shop_menu_input_field_id);

-- ============================================================
-- 8) INDEX 재생성 (새 이름)
-- ============================================================
CREATE INDEX idx_shop_menus_shop_active_sort
  ON shop_menus (shop_id, is_active, sort_order);

CREATE INDEX idx_shop_menu_tags_tag
  ON shop_menu_tags (tag_id, shop_menu_id);

CREATE INDEX idx_shop_menu_input_fields_menu
  ON shop_menu_input_fields (shop_menu_id, is_active, sort_order);

CREATE INDEX idx_reservation_menu_items_reservation
  ON reservation_menu_items (reservation_id, sort_order);

CREATE INDEX idx_reservation_menu_items_shop_menu
  ON reservation_menu_items (shop_menu_id);

CREATE INDEX idx_res_menu_input_values_menu_item
  ON reservation_menu_input_values (reservation_menu_item_id);

-- ============================================================
-- 9) FK 제약 재생성 (새 테이블/컬럼 참조)
-- ============================================================
ALTER TABLE reservation_menu_items
  ADD CONSTRAINT fk_rmi_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id);
ALTER TABLE reservation_menu_items
  ADD CONSTRAINT fk_rmi_shop_menu FOREIGN KEY (shop_menu_id) REFERENCES shop_menus(id);

ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_menu_item FOREIGN KEY (reservation_menu_item_id) REFERENCES reservation_menu_items(id);
ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_input_field FOREIGN KEY (shop_menu_input_field_id) REFERENCES shop_menu_input_fields(id);

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_shop_menu FOREIGN KEY (shop_menu_id) REFERENCES shop_menus(id);
ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_tag FOREIGN KEY (tag_id) REFERENCES tags(id);
