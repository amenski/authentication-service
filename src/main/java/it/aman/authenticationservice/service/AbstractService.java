package it.aman.authenticationservice.service;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.service.security.UserPrincipal;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AbstractService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String getCurrentLoggedInUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (Objects.nonNull(context.getAuthentication()) && Objects.nonNull(context.getAuthentication().getPrincipal())) {
            UserPrincipal principal = UserPrincipal.of(context.getAuthentication().getPrincipal());
            return principal.getUsername();
        }
        return null;
    }
}
