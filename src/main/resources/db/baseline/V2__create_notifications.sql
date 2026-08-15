CREATE TABLE notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  recipient_id BIGINT NOT NULL,
  actor_id BIGINT NOT NULL,
  notification_type VARCHAR(50) NOT NULL,
  title VARCHAR(100) NOT NULL,
  body VARCHAR(500) NOT NULL,
  reservation_id BIGINT NULL,
  shop_id BIGINT NULL,
  chat_room_id BIGINT NULL,
  message_id BIGINT NULL,
  read_at DATETIME(6) NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_notifications_recipient_message (recipient_id, message_id),
  KEY idx_notifications_recipient_id (recipient_id, id),
  KEY idx_notifications_recipient_unread (recipient_id, read_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
