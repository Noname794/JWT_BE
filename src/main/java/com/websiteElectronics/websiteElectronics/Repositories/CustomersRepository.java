package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomersRepository extends JpaRepository<Customers, Integer> {
    Optional<Customers> findByEmail(String email);
}
