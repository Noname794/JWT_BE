-- Script to insert sample OrderDetails for existing Orders
-- This creates 1-3 random products for each order

-- For Order 1
INSERT INTO order_details (order_id, product_id, quantity) VALUES (1, 1, 1);
INSERT INTO order_details (order_id, product_id, quantity) VALUES (1, 9, 1);

-- For Order 2  
INSERT INTO order_details (order_id, product_id, quantity) VALUES (2, 3, 1);

-- For Order 3
INSERT INTO order_details (order_id, product_id, quantity) VALUES (3, 5, 1);

-- For Order 4
INSERT INTO order_details (order_id, product_id, quantity) VALUES (4, 10, 1);

-- For Order 5
INSERT INTO order_details (order_id, product_id, quantity) VALUES (5, 8, 1);

-- For Order 6
INSERT INTO order_details (order_id, product_id, quantity) VALUES (6, 1, 1);
INSERT INTO order_details (order_id, product_id, quantity) VALUES (6, 3, 1);

-- For Order 14
INSERT INTO order_details (order_id, product_id, quantity) VALUES (14, 10, 2);

-- For Order 15
INSERT INTO order_details (order_id, product_id, quantity) VALUES (15, 9, 1);

-- For Order 16
INSERT INTO order_details (order_id, product_id, quantity) VALUES (16, 5, 1);

-- For Order 8
INSERT INTO order_details (order_id, product_id, quantity) VALUES (8, 1, 1);

-- For Order 9
INSERT INTO order_details (order_id, product_id, quantity) VALUES (9, 3, 1);

-- For Order 10
INSERT INTO order_details (order_id, product_id, quantity) VALUES (10, 8, 1);

-- For Order 11
INSERT INTO order_details (order_id, product_id, quantity) VALUES (11, 10, 3);

-- For Order 12
INSERT INTO order_details (order_id, product_id, quantity) VALUES (12, 9, 2);

-- For Order 13
INSERT INTO order_details (order_id, product_id, quantity) VALUES (13, 5, 1);
INSERT INTO order_details (order_id, product_id, quantity) VALUES (13, 1, 1);

-- For Order 17
INSERT INTO order_details (order_id, product_id, quantity) VALUES (17, 3, 2);

-- For Order 18
INSERT INTO order_details (order_id, product_id, quantity) VALUES (18, 8, 1);

-- For Order 19
INSERT INTO order_details (order_id, product_id, quantity) VALUES (19, 10, 1);

-- For Order 20
INSERT INTO order_details (order_id, product_id, quantity) VALUES (20, 9, 2);

-- For Order 21
INSERT INTO order_details (order_id, product_id, quantity) VALUES (21, 5, 1);

-- For Order 22
INSERT INTO order_details (order_id, product_id, quantity) VALUES (22, 1, 1);
INSERT INTO order_details (order_id, product_id, quantity) VALUES (22, 3, 1);

-- Verify the inserts
SELECT COUNT(*) as total_order_details FROM order_details;
SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity 
FROM order_details od 
ORDER BY od.order_id;
