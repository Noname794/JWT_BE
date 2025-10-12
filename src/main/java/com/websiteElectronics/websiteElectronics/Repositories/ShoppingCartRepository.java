package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {
    List<ShoppingCart> findByCustomerId(Integer customerId);
}
