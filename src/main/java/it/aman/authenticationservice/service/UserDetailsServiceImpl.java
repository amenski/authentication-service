package it.aman.authenticationservice.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.dal.entity.AuthAccount;
import it.aman.authenticationservice.dal.entity.AuthRole;
import it.aman.authenticationservice.dal.repository.AccountRepository;
import it.aman.authenticationservice.service.security.UserPrincipal;
import it.aman.authenticationservice.util.AuthConstants;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountRepository accRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        final AuthAccount acc = accRepository.findByEmail(username);
        if (acc == null) {
            throw new UsernameNotFoundException(AuthConstants.ACCOUNT_NOT_FOUND);
        }

        Function<AuthRole, List<GrantedAuthority>> rolesAndPermissions = role -> {
            List<GrantedAuthority> list = new ArrayList<>();
            list.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (String authority : role.getAuthority()) {
                list.add(new SimpleGrantedAuthority(authority));
            }
            return list;
        };

        return new UserPrincipal(
                acc.getEmail(), 
                acc.getPassword(), 
                acc.isEnabled(), 
                true, true, true,
                acc.getEpsRoles().stream().map(rolesAndPermissions)
                        .collect(Collectors.flatMapping(Collection::stream, Collectors.toList())));
    }

}
