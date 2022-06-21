package it.aman.authenticationservice.service.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal extends User {

    private static final long serialVersionUID = 5801030335742971708L;

    public UserPrincipal(String username,
                            String password,
                            boolean enabled,
                            boolean accountNonExpired,
                            boolean credentialsNonExpired,
                            boolean accountNonLocked,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }
    
    public static UserPrincipal of(Object object) {
    	try {
    		return (UserPrincipal) object;
    	} catch(ClassCastException e) {
    		//niente
    	}
    	//anonymous user
    	return new UserPrincipal((String) object, "", false, false, false, false, Collections.emptyList());
    }
}
