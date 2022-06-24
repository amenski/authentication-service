package it.aman.authenticationservice.aspect;

import java.util.Objects;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import it.aman.authenticationservice.service.security.UserPrincipal;

/**
 * Will be extended by the different aspects requiring some common
 * functionalities
 * 
 * @author Aman
 *
 */
@Component
public class CommonAspectComponent {

    protected UserPrincipal getLoggedInUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (Objects.nonNull(context.getAuthentication()) && Objects.nonNull(context.getAuthentication().getPrincipal())) {
            return UserPrincipal.of(context.getAuthentication().getPrincipal());
        }
        return null;
    }
}
