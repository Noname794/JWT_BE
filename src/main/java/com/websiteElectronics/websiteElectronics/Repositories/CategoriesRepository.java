package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.Categories;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriesRepository extends JpaRepository<Categories, Integer> {
}
