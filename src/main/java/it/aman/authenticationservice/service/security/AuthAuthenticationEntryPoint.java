package it.aman.authenticationservice.service.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The main function of AuthenticationEntryPoint is to allow the framework
 * to send some sort of "to access this resource you must authenticate first"
 * notification from application server to web client. i.e. this applies 
 * only for users not yet authenticated(anonymousUser).
 * <p>
 * So, this class just returns HTTP code 401 (Unauthorized) when authentication fails,
 * overriding default Spring’s redirection.
 *
 * @author Aman
 */
@Component
public class AuthAuthenticationEntryPoint implements AuthenticationEntryPoint {

	/**
	 * With the help of this resolver we can throw an exception which will be caught by {@code @ExceptionHandler}<br/>
	 * Throws {@code InsufficientAuthenticationException}<br/>
	 * 
	 * https://stackoverflow.com/a/46530064/2660789
	 */
	private final HandlerExceptionResolver resolver;

    public AuthAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") final HandlerExceptionResolver resolver) {
		this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        resolver.resolveException(request, response, null, authException);
    }
}
