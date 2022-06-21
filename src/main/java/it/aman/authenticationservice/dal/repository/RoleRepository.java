package it.aman.authenticationservice.dal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import it.aman.authenticationservice.dal.entity.AuthRole;
import it.aman.authenticationservice.dal.entity.AuthUser;

@Repository
public interface RoleRepository extends JpaRepository<AuthRole, Integer>, JpaSpecificationExecutor<AuthUser> {

	Optional<AuthRole> findByName(String roleName);
}
