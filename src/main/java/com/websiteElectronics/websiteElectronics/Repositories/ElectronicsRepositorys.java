package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ElectronicsRepositorys extends JpaRepository<Products, Integer> {
    List<Products> findByIdIn(List<Integer> ids);
}
