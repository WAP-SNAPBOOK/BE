CREATE TABLE shop_settings (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  interval_minutes INT NOT NULL DEFAULT 30,
  schedule_type VARCHAR(30) NOT NULL DEFAULT 'DAILY',
  booking_window_days INT NOT NULL DEFAULT 30,
  min_booking_lead_minutes INT NOT NULL DEFAULT 60,
  public_holiday_off BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_shop_settings_shop_id (shop_id)
);

CREATE TABLE shop_operating_times (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  day_of_week VARCHAR(10) NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_shop_operating_times_shop_day (shop_id, day_of_week, sort_order)
);

CREATE TABLE staff_operating_times (
  id BIGINT NOT NULL AUTO_INCREMENT,
  staff_id BIGINT NOT NULL,
  day_of_week VARCHAR(10) NOT NULL,
  is_off BOOLEAN NOT NULL DEFAULT FALSE,
  start_time TIME NULL,
  end_time TIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_staff_operating_times_staff_day (staff_id, day_of_week)
);

CREATE TABLE shop_holidays (
  id BIGINT NOT NULL AUTO_INCREMENT,
  shop_id BIGINT NOT NULL,
  holiday_type VARCHAR(20) NOT NULL,
  day_of_week VARCHAR(10) NULL,
  week_of_month INT NULL,
  reference_date DATE NULL,
  specific_date DATE NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_shop_holidays_shop (shop_id)
);

CREATE TABLE public_holidays (
  id BIGINT NOT NULL AUTO_INCREMENT,
  holiday_date DATE NOT NULL,
  name VARCHAR(100) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_public_holidays_holiday_date (holiday_date)
);
