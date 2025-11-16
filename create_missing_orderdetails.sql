-- Script to create OrderDetails for Orders that don't have any
-- This will create 1-2 random products for each order without orderdetails

-- Step 1: Check current status
SELECT 
    (SELECT COUNT(*) FROM orders) as total_orders,
    (SELECT COUNT(DISTINCT order_id) FROM orderdetails) as orders_with_details,
    (SELECT COUNT(*) FROM orders WHERE order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails)) as orders_without_details;

-- Step 2: Insert OrderDetails for orders that don't have any
-- Each order will get 1-2 random products with quantity 1

INSERT INTO orderdetails (order_id, product_id, quantity)
SELECT 
    o.order_id,
    -- Random product_id between 1 and 98 (adjust based on your products)
    FLOOR(1 + RAND() * 98) as product_id,
    1 as quantity
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails)
AND EXISTS (SELECT 1 FROM products p WHERE p.product_id = FLOOR(1 + RAND() * 98));

-- Optional: Add a second product for some orders (50% chance)
INSERT INTO orderdetails (order_id, product_id, quantity)
SELECT 
    o.order_id,
    FLOOR(1 + RAND() * 98) as product_id,
    1 as quantity
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails WHERE order_id = o.order_id HAVING COUNT(*) > 1)
AND RAND() > 0.5
AND EXISTS (SELECT 1 FROM products p WHERE p.product_id = FLOOR(1 + RAND() * 98))
LIMIT (SELECT COUNT(*) FROM orders WHERE order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails)) / 2;

-- Step 3: Verify the results
SELECT 
    'After Insert' as status,
    (SELECT COUNT(*) FROM orders) as total_orders,
    (SELECT COUNT(DISTINCT order_id) FROM orderdetails) as orders_with_details,
    (SELECT COUNT(*) FROM orderdetails) as total_orderdetails;

-- Step 4: Show sample data
SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity, p.name as product_name
FROM orderdetails od
LEFT JOIN products p ON od.product_id = p.product_id
ORDER BY od.order_id
LIMIT 20;
