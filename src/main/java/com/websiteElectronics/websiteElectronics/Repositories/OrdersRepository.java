package com.websiteElectronics.websiteElectronics.Repositories;
import com.websiteElectronics.websiteElectronics.Entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;



public interface OrdersRepository extends JpaRepository<Orders, Integer> {
    @Query(value = "SELECT COUNT(order_id), SUM(total_amount) FROM orders WHERE customer_id = :customerId", nativeQuery = true)
    List<Object[]> findOderStatsByCustomerId(@Param("customerId") Integer customerId);
}


