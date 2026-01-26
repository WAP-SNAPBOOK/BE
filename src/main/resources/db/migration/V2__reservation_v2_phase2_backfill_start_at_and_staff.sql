-- 예약 ERD v2 Phase 2: backfill (start_at, staff)
-- Created: 2026-01-23

-- 1) date + time -> start_at
UPDATE reservations
SET start_at = TIMESTAMP(`date`, `time`)
WHERE start_at IS NULL
  AND `date` IS NOT NULL
  AND `time` IS NOT NULL;

-- 2) 기본 staff 생성 (기존 예약 기준: shop_id + owner_user_id 조합)
-- 주의: 이미 staff가 있을 수 있으므로 중복 삽입 방지를 위해 NOT EXISTS 사용
INSERT INTO staff (shop_id, user_id, name, created_at, updated_at)
SELECT r.shop_id, r.owner_user_id, NULL, NOW(), NOW()
FROM reservations r
WHERE r.shop_id IS NOT NULL
  AND r.owner_user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM staff s
    WHERE s.shop_id = r.shop_id AND s.user_id = r.owner_user_id
  )
GROUP BY r.shop_id, r.owner_user_id;

-- 3) reservations.staff_id 채우기
UPDATE reservations r
JOIN staff s
  ON s.shop_id = r.shop_id
 AND s.user_id = r.owner_user_id
SET r.staff_id = s.id
WHERE r.staff_id IS NULL;

