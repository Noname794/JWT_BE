-- Super simple script: Create 1 orderdetail for each order that doesn't have any
-- Run this in MySQL Workbench or phpMyAdmin

-- Step 1: See which orders don't have orderdetails
SELECT o.order_id 
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails);

-- Step 2: Insert orderdetails for those orders
-- Each order gets product_id based on (order_id MOD 10) + 1
INSERT INTO orderdetails (order_id, product_id, quantity)
SELECT 
    o.order_id,
    MOD(o.order_id - 1, 10) + 1,
    1
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails);

-- Step 3: Check results
SELECT COUNT(*) as total_orderdetails FROM orderdetails;

SELECT od.* FROM orderdetails od ORDER BY od.order_id;
