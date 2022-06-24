package it.aman.authenticationservice.dal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthUser;
import it.aman.authenticationservice.dal.util.HibernateRepository;

@Repository
public interface UserRepository
        extends JpaRepository<AuthUser, Integer>, JpaSpecificationExecutor<AuthUser>, HibernateRepository<AuthUser> {

    Optional<AuthUser> findByAccountId(Integer id);

    Optional<AuthUser> findByAccountEmail(String username);

    boolean existsByAccountEmail(String username);
}
