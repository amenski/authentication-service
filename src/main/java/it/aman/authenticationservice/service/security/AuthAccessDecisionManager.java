package it.aman.authenticationservice.service.security;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

public class AuthAccessDecisionManager extends AbstractAccessDecisionManager {

    private final Logger log = LoggerFactory.getLogger(AuthAccessDecisionManager.class);
    
    public AuthAccessDecisionManager(List<AccessDecisionVoter<?>> decisionVoters) {
        super(decisionVoters);
    }
    
    /**
     * Will not vote if configAttributes is empty. i.e. if we dont have a record in db
     * 
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked"})
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes)
            throws AccessDeniedException, InsufficientAuthenticationException {
        for (AccessDecisionVoter voter : getDecisionVoters()) {
            int result = voter.vote(authentication, object, configAttributes);
            switch (result) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    return;
                case AccessDecisionVoter.ACCESS_DENIED:
                    log.error("Access denied for user: {}", authentication.getPrincipal());
                    throw new AccessDeniedException(messages.getMessage("AbstractAccessDecisionManager.accessDenied", "Access is denied"));
                default:
                    // abstain
                    break;
            }
        }
    }

}
