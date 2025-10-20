package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Model.CustomersBehaviour;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomersBehaviourRepository extends MongoRepository<CustomersBehaviour, Integer> {
}
