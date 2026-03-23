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
JOIN shop_menus sm ON sm.id = smt.shop_menu_id
JOIN tags t ON t.id = smt.tag_id
JOIN shop_tags st ON st.shop_id = sm.shop_id AND st.name = t.name
SET smt.shop_tag_id = st.id
WHERE smt.shop_tag_id IS NULL;
