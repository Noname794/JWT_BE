package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    @Query("SELECT od.productId.id " +
            "FROM OrderDetails od " +
            "WHERE od.orderId.customer.id = :customerId")
    List<Integer> findProductIdsByCustomerId(@Param("customerId") Integer customerId);

    List<OrderDetails> findByOrderId_Id(int orderId);
}
