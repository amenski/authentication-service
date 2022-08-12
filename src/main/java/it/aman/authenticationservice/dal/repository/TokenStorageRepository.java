package it.aman.authenticationservice.dal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthTokenStorage;
import it.aman.common.dal.HibernateRepository;

@Repository
public interface TokenStorageRepository extends JpaRepository<AuthTokenStorage, Integer>, HibernateRepository<AuthTokenStorage>  {
    
    Optional<AuthTokenStorage> findByRefreshToken(String refreshToken);
}
