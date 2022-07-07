package it.aman.authenticationservice.dal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthTokenStorage;

@Repository
public interface TokenStorageRepository extends JpaRepository<AuthTokenStorage, String>  {
    
    Optional<AuthTokenStorage> findByTokenString(String token);
}
