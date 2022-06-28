package it.aman.authenticationservice.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthAccount;
import it.aman.common.dal.HibernateRepository;

@Repository
public interface AccountRepository extends JpaRepository<AuthAccount, Integer> {
    AuthAccount findByEmail(String email);
}
