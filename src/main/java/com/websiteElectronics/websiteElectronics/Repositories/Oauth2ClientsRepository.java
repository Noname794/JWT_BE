package com.websiteElectronics.websiteElectronics.Repositories;

import com.websiteElectronics.websiteElectronics.Entities.Oauth2Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Oauth2ClientsRepository extends JpaRepository<Oauth2Clients, String> {
    Optional<Oauth2Clients> findByClientId(String clientId);
}
