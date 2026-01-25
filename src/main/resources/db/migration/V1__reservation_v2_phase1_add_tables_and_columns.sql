-- 예약 ERD v2 Phase 1: 파괴적 변경 없이 "추가만"
-- Created: 2026-01-23

-- 1) reservations 확장 컬럼 (초기에는 NULL 허용)
ALTER TABLE reservations
  ADD COLUMN staff_id BIGINT NULL,
  ADD COLUMN start_at DATETIME NULL,
  ADD COLUMN duration_minutes INT NULL,
  ADD COLUMN customer_note TEXT NULL,
  ADD COLUMN updated_at DATETIME NULL,
  ADD COLUMN deleted_at DATETIME NULL;

-- 2) staff (복수 직원 지원)
CREATE TABLE staff (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  user_id BIGINT NULL,
  name VARCHAR(100) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_staff_shop_id (shop_id),
  INDEX idx_staff_user_id (user_id)
);

-- 3) reservation_time_blocks (CONFIRMED 점유 블록)
CREATE TABLE reservation_time_blocks (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  block_start_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_rtb_reservation_id (reservation_id),
  INDEX idx_rtb_staff_block_start_at (staff_id, block_start_at)
);

-- 4) shop_services (매장별 메뉴)
CREATE TABLE shop_services (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT NULL,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_services_shop_name (shop_id, name),
  INDEX idx_shop_services_shop_active_sort (shop_id, is_active, sort_order)
);

-- 5) tags + shop_service_tags (메뉴 태그)
CREATE TABLE tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tags_name (name)
);

CREATE TABLE shop_service_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_service_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_service_tags (shop_service_id, tag_id),
  INDEX idx_shop_service_tags_tag (tag_id, shop_service_id)
);

-- 6) shop_service_input_fields (메뉴별 추가 입력 필드)
CREATE TABLE shop_service_input_fields (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_service_id BIGINT NOT NULL,
  `key` VARCHAR(100) NOT NULL,
  label VARCHAR(255) NOT NULL,
  input_type VARCHAR(20) NOT NULL, -- NUMBER | TEXT
  required BOOLEAN NOT NULL DEFAULT FALSE,
  min_value DECIMAL(10,2) NULL,
  max_value DECIMAL(10,2) NULL,
  step_value DECIMAL(10,2) NULL,
  max_length INT NULL,
  placeholder VARCHAR(255) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_service_input_fields (shop_service_id, `key`),
  INDEX idx_shop_service_input_fields_service (shop_service_id, is_active, sort_order)
);

-- 7) reservation_services (예약-메뉴 다중 선택 + 스냅샷)
CREATE TABLE reservation_services (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  shop_service_id BIGINT NOT NULL,
  service_name_snapshot VARCHAR(255) NOT NULL,
  service_description_snapshot TEXT NULL,
  service_duration_snapshot_minutes INT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_reservation_services_reservation (reservation_id, sort_order),
  INDEX idx_reservation_services_shop_service (shop_service_id)
);

-- 8) reservation_menu_input_values (예약-메뉴별 추가 입력값 + 스냅샷)
CREATE TABLE reservation_menu_input_values (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_service_id BIGINT NOT NULL,
  shop_service_input_field_id BIGINT NOT NULL,
  field_key_snapshot VARCHAR(100) NOT NULL,
  field_label_snapshot VARCHAR(255) NOT NULL,
  input_type_snapshot VARCHAR(20) NOT NULL, -- NUMBER | TEXT
  value_number DECIMAL(10,2) NULL,
  value_text TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_res_menu_input_values_res_service (reservation_service_id)
);

-- 9) 이력 테이블(상태/변경)
CREATE TABLE reservation_status_histories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  from_status VARCHAR(20) NULL,
  to_status VARCHAR(20) NOT NULL,
  changed_by_user_id BIGINT NOT NULL,
  reason TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_rsh_reservation (reservation_id, created_at)
);

CREATE TABLE reservation_change_histories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  changed_by_user_id BIGINT NOT NULL,
  change_type VARCHAR(50) NOT NULL,
  before_json LONGTEXT NULL,
  after_json LONGTEXT NULL,
  reason TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_rch_reservation (reservation_id, created_at)
);

