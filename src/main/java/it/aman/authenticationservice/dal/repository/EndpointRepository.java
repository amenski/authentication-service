package it.aman.authenticationservice.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.dal.entity.AuthEndpoint.AuthEndpointId;

@Repository
public interface EndpointRepository extends JpaRepository<AuthEndpoint, AuthEndpointId>  {
}
