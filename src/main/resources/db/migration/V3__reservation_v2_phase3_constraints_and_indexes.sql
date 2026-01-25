-- 예약 ERD v2 Phase 3: 제약/인덱스 적용
-- Created: 2026-01-23

-- 1) 점유 겹침 방지(확정 시점)
ALTER TABLE reservation_time_blocks
  ADD CONSTRAINT uq_rtb_staff_block_start_at UNIQUE (staff_id, block_start_at);

-- 2) 동일 메뉴 중복 선택 금지(확정)
ALTER TABLE reservation_services
  ADD CONSTRAINT uq_reservation_services_res_shop_service UNIQUE (reservation_id, shop_service_id);

-- 3) 메뉴별 입력값: 동일 필드 중복 입력 방지
ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT uq_res_menu_input_values UNIQUE (reservation_service_id, shop_service_input_field_id);

-- 4) 조회 성능 인덱스(권장)
CREATE INDEX idx_res_shop_status_start_at ON reservations (shop_id, status, start_at);
CREATE INDEX idx_res_staff_start_at ON reservations (staff_id, start_at);
CREATE INDEX idx_res_customer_created_at ON reservations (customer_id, created_at);

-- 5) 내부 FK(신규 테이블끼리) — 기존 테이블(shop/users 등)로의 FK는 스키마 확인 후 추가하는 것을 권장
ALTER TABLE reservation_time_blocks
  ADD CONSTRAINT fk_rtb_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id);
ALTER TABLE reservation_time_blocks
  ADD CONSTRAINT fk_rtb_staff FOREIGN KEY (staff_id) REFERENCES staff(id);

ALTER TABLE reservation_services
  ADD CONSTRAINT fk_res_services_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id);
ALTER TABLE reservation_services
  ADD CONSTRAINT fk_res_services_shop_service FOREIGN KEY (shop_service_id) REFERENCES shop_services(id);

ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_res_service FOREIGN KEY (reservation_service_id) REFERENCES reservation_services(id);
ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_input_field FOREIGN KEY (shop_service_input_field_id) REFERENCES shop_service_input_fields(id);

ALTER TABLE shop_service_tags
  ADD CONSTRAINT fk_sst_shop_service FOREIGN KEY (shop_service_id) REFERENCES shop_services(id);
ALTER TABLE shop_service_tags
  ADD CONSTRAINT fk_sst_tag FOREIGN KEY (tag_id) REFERENCES tags(id);

