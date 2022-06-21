package it.aman.authenticationservice.service.security;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.RoleVoter;

public class AuthRoleVoter extends RoleVoter {

    /**
     * Removes the extra check on ROLE_PREFIX
     * 
     */
    @Override
    public boolean supports(ConfigAttribute attribute) {
        return attribute.getAttribute() != null;
    }

    
}
