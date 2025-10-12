package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewsRepository extends JpaRepository<Reviews, Integer> {
}
