package it.aman.authenticationservice.service.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import it.aman.authenticationservice.util.AuthConstants;
import lombok.RequiredArgsConstructor;

/**
 * If valid jwt found, populate security context. Or else forward to the next filter. 
 * 
 * @author Amanuel
 *
 */
@Component
@RequiredArgsConstructor
public class JwtCustomRequestAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        String header = httpServletRequest.getHeader(AuthConstants.AUTH_HEADER_STRING);
        String username = null;
        String authToken = null;
        if (header != null && header.startsWith(AuthConstants.AUTH_TOKEN_PREFIX)) {
            authToken = header.replace(AuthConstants.AUTH_TOKEN_PREFIX, "");
            try {
                username = jwtTokenUtil.extractUsername(authToken);
            } catch (IllegalArgumentException e) {
                logger.error("An error occured during getting username from token", e);
            } catch (JwtException e) {
                logger.error("The token is expired and not valid anymore", e);
            }
        } else {
            logger.warn("Couldn't find bearer string, will ignore the header");
        }
        if (!StringUtils.isBlank(username) && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);

            if (Boolean.TRUE.equals(jwtTokenUtil.verifyToken(authToken, userDetails))) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                logger.info(String.format("Setting security context for authenticated user %s", username));
                final SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
