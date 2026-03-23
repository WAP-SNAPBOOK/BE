INSERT INTO tags (id, name)
SELECT 1, '손관리'
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 1);

INSERT INTO tags (id, name)
SELECT 2, '발관리'
WHERE NOT EXISTS (SELECT 1 FROM tags WHERE id = 2);

INSERT INTO shop_menus (id, shop_id, name, is_active, sort_order)
SELECT 1, 10, '젤네일', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM shop_menus WHERE id = 1);

INSERT INTO shop_menus (id, shop_id, name, is_active, sort_order)
SELECT 2, 10, '페디큐어', TRUE, 1
WHERE NOT EXISTS (SELECT 1 FROM shop_menus WHERE id = 2);

INSERT INTO shop_menus (id, shop_id, name, is_active, sort_order)
SELECT 3, 20, '타매장젤', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM shop_menus WHERE id = 3);

INSERT INTO shop_menu_tags (id, shop_menu_id, tag_id)
SELECT 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM shop_menu_tags WHERE id = 1);

INSERT INTO shop_menu_tags (id, shop_menu_id, tag_id)
SELECT 2, 2, 2
WHERE NOT EXISTS (SELECT 1 FROM shop_menu_tags WHERE id = 2);

INSERT INTO shop_menu_tags (id, shop_menu_id, tag_id)
SELECT 3, 3, 1
WHERE NOT EXISTS (SELECT 1 FROM shop_menu_tags WHERE id = 3);

INSERT INTO shop_tags (shop_id, name, sort_order)
SELECT missing.shop_id,
       missing.name,
       COALESCE(existing.max_sort_order, -1)
           + ROW_NUMBER() OVER (PARTITION BY missing.shop_id ORDER BY missing.min_tag_id) AS sort_order
FROM (
  SELECT sm.shop_id AS shop_id,
         t.name AS name,
         MIN(t.id) AS min_tag_id
  FROM shop_menu_tags smt
  JOIN shop_menus sm ON sm.id = smt.shop_menu_id
  JOIN tags t ON t.id = smt.tag_id
  LEFT JOIN shop_tags st ON st.shop_id = sm.shop_id AND st.name = t.name
  WHERE st.id IS NULL
  GROUP BY sm.shop_id, t.name
) missing
LEFT JOIN (
  SELECT shop_id, MAX(sort_order) AS max_sort_order
  FROM shop_tags
  GROUP BY shop_id
) existing ON existing.shop_id = missing.shop_id;

UPDATE shop_menu_tags smt
SET shop_tag_id = (
  SELECT st.id
  FROM shop_menus sm
  JOIN tags t ON t.id = smt.tag_id
  JOIN shop_tags st ON st.shop_id = sm.shop_id AND st.name = t.name
  WHERE sm.id = smt.shop_menu_id
)
WHERE smt.shop_tag_id IS NULL;
