-- Quick fix: Create orderdetails for all existing orders
-- Run this in MySQL Workbench

USE electronics_websites_db;

-- Insert 1 orderdetail for each order (cycling through products 1-10)
INSERT INTO orderdetails (order_id, product_id, quantity)
SELECT 
    o.order_id,
    MOD(o.order_id - 1, 10) + 1 as product_id,
    1 as quantity
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails);

-- Check results
SELECT COUNT(*) as total_orderdetails FROM orderdetails;
