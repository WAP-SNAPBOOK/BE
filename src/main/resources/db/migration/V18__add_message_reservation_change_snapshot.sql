ALTER TABLE message
  ADD COLUMN reservation_start_at_before TIME NULL,
  ADD COLUMN reservation_start_at_after TIME NULL,
  ADD COLUMN reservation_duration_minutes_before INT NULL,
  ADD COLUMN reservation_duration_minutes_after INT NULL,
  ADD COLUMN reservation_staff_id_before BIGINT NULL,
  ADD COLUMN reservation_staff_name_before VARCHAR(100) NULL,
  ADD COLUMN reservation_staff_id_after BIGINT NULL,
  ADD COLUMN reservation_staff_name_after VARCHAR(100) NULL,
  ADD COLUMN owner_message TEXT NULL;
