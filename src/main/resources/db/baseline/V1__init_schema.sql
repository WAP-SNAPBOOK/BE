CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_date DATETIME(6) NULL,
  name VARCHAR(255) NOT NULL,
  phone_number VARCHAR(255) NOT NULL,
  provider_id VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','USER') NULL,
  user_type ENUM('CUSTOMER','OWNER') NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_users_provider_id (provider_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop (
  id BIGINT NOT NULL AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  public_code VARCHAR(20) NULL,
  slug VARCHAR(50) NULL,
  address VARCHAR(255) NULL,
  business_name VARCHAR(255) NOT NULL,
  business_number VARCHAR(255) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_shop_owner_id (owner_id),
  UNIQUE KEY UK_shop_public_code (public_code),
  UNIQUE KEY UK_shop_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE staff (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_staff_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  date DATE NOT NULL,
  time TIME(6) NOT NULL,
  start_at DATETIME(6) NULL,
  duration_minutes INT NULL,
  shop_id BIGINT NOT NULL,
  owner_user_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  staff_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL,
  requirements TEXT NULL,
  rejection_reason VARCHAR(255) NULL,
  confirmation_message VARCHAR(255) NULL,
  canceled_by_type ENUM('CUSTOMER','OWNER') NULL,
  cancel_timing ENUM('AFTER_CUTOFF','BEFORE_CUTOFF') NULL,
  canceled_by_user_id BIGINT NULL,
  canceled_at DATETIME(6) NULL,
  cancel_reason TEXT NULL,
  refund_eligible BIT(1) NULL,
  status ENUM('CANCELED','CONFIRMED','PENDING','REJECTED') NOT NULL,
  PRIMARY KEY (id),
  KEY idx_res_shop_status_start_at (shop_id, status, start_at),
  KEY idx_res_staff_start_at (staff_id, start_at),
  KEY idx_res_customer_created_at (customer_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_photos (
  reservation_id BIGINT NOT NULL,
  photo_url VARCHAR(255) NULL,
  KEY idx_reservation_photos_reservation_id (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_time_blocks (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  block_start_at DATETIME(6) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rtb_staff_block_start_at (staff_id, block_start_at),
  KEY idx_rtb_reservation_id (reservation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_status_histories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  from_status ENUM('CANCELED','CONFIRMED','PENDING','REJECTED') NULL,
  to_status ENUM('CANCELED','CONFIRMED','PENDING','REJECTED') NOT NULL,
  changed_by_user_id BIGINT NOT NULL,
  reason TEXT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_rsh_reservation (reservation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_change_histories (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  changed_by_user_id BIGINT NOT NULL,
  change_type VARCHAR(50) NOT NULL,
  before_json LONGTEXT NULL,
  after_json LONGTEXT NULL,
  reason TEXT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_rch_reservation (reservation_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_tags_shop_name (shop_id, name),
  UNIQUE KEY uq_shop_tags_shop_sort (shop_id, sort_order),
  KEY idx_shop_tags_shop_sort (shop_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_menus (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NULL,
  price BIGINT NULL,
  is_active BIT(1) NOT NULL,
  sort_order INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  deleted_at DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_menus_shop_name (shop_id, name),
  KEY idx_shop_menus_shop_active_sort (shop_id, is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_menu_input_fields (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_menu_id BIGINT NOT NULL,
  label VARCHAR(255) NOT NULL,
  input_type ENUM('NUMBER','TEXT') NOT NULL,
  required BIT(1) NOT NULL,
  min_value DECIMAL(10,2) NULL,
  max_value DECIMAL(10,2) NULL,
  step_value DECIMAL(10,2) NULL,
  max_length INT NULL,
  placeholder VARCHAR(255) NULL,
  sort_order INT NOT NULL,
  is_active BIT(1) NOT NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_menu_input_fields (shop_menu_id, label),
  KEY idx_shop_menu_input_fields_menu (shop_menu_id, is_active, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_menu_tags (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_menu_id BIGINT NOT NULL,
  tag_id BIGINT NULL,
  shop_tag_id BIGINT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_menu_tags (shop_menu_id, tag_id),
  UNIQUE KEY uq_shop_menu_tags_shop_tag (shop_menu_id, shop_tag_id),
  KEY idx_shop_menu_tags_tag (tag_id, shop_menu_id),
  KEY idx_shop_menu_tags_shop_tag (shop_tag_id, shop_menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_menu_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT NOT NULL,
  shop_menu_id BIGINT NOT NULL,
  menu_name_snapshot VARCHAR(255) NOT NULL,
  tag_name_snapshot VARCHAR(255) NULL,
  price_snapshot BIGINT NULL,
  sort_order INT NOT NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_reservation_menu_items_res_shop_menu (reservation_id, shop_menu_id),
  KEY idx_reservation_menu_items_reservation (reservation_id, sort_order),
  KEY idx_reservation_menu_items_shop_menu (shop_menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE reservation_menu_input_values (
  id BIGINT NOT NULL AUTO_INCREMENT,
  reservation_menu_item_id BIGINT NOT NULL,
  shop_menu_input_field_id BIGINT NOT NULL,
  field_label_snapshot VARCHAR(255) NOT NULL,
  input_type_snapshot VARCHAR(20) NOT NULL,
  value_number DECIMAL(10,2) NULL,
  value_text VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_res_menu_input_values (reservation_menu_item_id, shop_menu_input_field_id),
  KEY idx_res_menu_input_values_menu_item (reservation_menu_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_settings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  interval_minutes INT NOT NULL,
  schedule_type ENUM('BY_DAY','DAILY','WEEKDAY_WEEKEND') NOT NULL,
  booking_window_days INT NOT NULL,
  min_booking_lead_minutes INT NOT NULL,
  public_holiday_off BIT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_shop_settings_shop_id (shop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_operating_times (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  day_of_week ENUM('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  start_time TIME(6) NOT NULL,
  end_time TIME(6) NOT NULL,
  sort_order INT NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE staff_operating_times (
  id BIGINT NOT NULL AUTO_INCREMENT,
  staff_id BIGINT NOT NULL,
  day_of_week ENUM('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
  is_off BIT(1) NOT NULL,
  start_time TIME(6) NULL,
  end_time TIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_staff_operating_times_staff_day (staff_id, day_of_week)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE shop_holidays (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  holiday_type ENUM('BIWEEKLY','CUSTOM','MONTHLY','WEEKLY') NOT NULL,
  day_of_week ENUM('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NULL,
  week_of_month INT NULL,
  reference_date DATE NULL,
  specific_date DATE NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE public_holidays (
  id BIGINT NOT NULL AUTO_INCREMENT,
  holiday_date DATE NOT NULL,
  name VARCHAR(100) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_public_holidays_holiday_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE chat_room (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  owner_id BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  owner_last_read_message_id BIGINT NULL,
  customer_last_read_message_id BIGINT NULL,
  last_message_id BIGINT NULL,
  last_message_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  chat_room_id BIGINT NOT NULL,
  sender_id BIGINT NOT NULL,
  content TEXT NULL,
  sent_at DATETIME(6) NOT NULL,
  image_url VARCHAR(500) NULL,
  message_type ENUM('IMAGE','RESERVATION_CANCELED','RESERVATION_CONFIRMED','RESERVATION_CREATED','RESERVATION_REJECTED','RESERVATION_UPDATED','TEXT','TEXT_IMAGE') NULL,
  reservation_id BIGINT NULL,
  duration_minutes INT NULL,
  reservation_start_at_before TIME(6) NULL,
  reservation_start_at_after TIME(6) NULL,
  reservation_date_before DATE NULL,
  reservation_date_after DATE NULL,
  reservation_duration_minutes_before INT NULL,
  reservation_duration_minutes_after INT NULL,
  reservation_staff_id_before BIGINT NULL,
  reservation_staff_name_before VARCHAR(100) NULL,
  reservation_staff_id_after BIGINT NULL,
  reservation_staff_name_after VARCHAR(100) NULL,
  owner_message TEXT NULL,
  owner_message_before TEXT NULL,
  reservation_menus_before TEXT NULL,
  reservation_menus_after TEXT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE reservation_photos
  ADD CONSTRAINT fk_reservation_photos_reservation
  FOREIGN KEY (reservation_id) REFERENCES reservations(id);

ALTER TABLE reservation_time_blocks
  ADD CONSTRAINT fk_rtb_reservation
  FOREIGN KEY (reservation_id) REFERENCES reservations(id);

ALTER TABLE reservation_time_blocks
  ADD CONSTRAINT fk_rtb_staff
  FOREIGN KEY (staff_id) REFERENCES staff(id);

ALTER TABLE reservation_menu_items
  ADD CONSTRAINT fk_rmi_reservation
  FOREIGN KEY (reservation_id) REFERENCES reservations(id);

ALTER TABLE reservation_menu_items
  ADD CONSTRAINT fk_rmi_shop_menu
  FOREIGN KEY (shop_menu_id) REFERENCES shop_menus(id);

ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_menu_item
  FOREIGN KEY (reservation_menu_item_id) REFERENCES reservation_menu_items(id);

ALTER TABLE reservation_menu_input_values
  ADD CONSTRAINT fk_rmiv_input_field
  FOREIGN KEY (shop_menu_input_field_id) REFERENCES shop_menu_input_fields(id);

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_shop_menu
  FOREIGN KEY (shop_menu_id) REFERENCES shop_menus(id);

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_tag
  FOREIGN KEY (tag_id) REFERENCES tags(id);

ALTER TABLE shop_menu_tags
  ADD CONSTRAINT fk_smt_shop_tag
  FOREIGN KEY (shop_tag_id) REFERENCES shop_tags(id);
