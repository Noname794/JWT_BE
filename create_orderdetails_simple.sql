-- Simple script to create OrderDetails for all Orders
-- Each order will get 1 product (cycling through product IDs)

-- Step 1: Check what orders don't have orderdetails
SELECT o.order_id, o.customer_id, o.total_amount, o.status
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails)
ORDER BY o.order_id;

-- Step 2: Insert OrderDetails for missing orders
-- This uses MOD to cycle through products 1-10 (you can adjust)
INSERT INTO orderdetails (order_id, product_id, quantity)
SELECT 
    o.order_id,
    ((o.order_id - 1) MOD 10) + 1 as product_id,  -- Cycles through products 1-10
    1 as quantity
FROM orders o
WHERE o.order_id NOT IN (SELECT DISTINCT order_id FROM orderdetails);

-- Step 3: Verify
SELECT 
    'Summary' as info,
    COUNT(DISTINCT o.order_id) as total_orders,
    COUNT(DISTINCT od.order_id) as orders_with_details,
    COUNT(*) as total_orderdetails
FROM orders o
LEFT JOIN orderdetails od ON o.order_id = od.order_id;

-- Step 4: Show all orderdetails
SELECT od.order_detail_id, od.order_id, od.product_id, od.quantity
FROM orderdetails od
ORDER BY od.order_id;
