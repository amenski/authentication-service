package it.aman.authenticationservice.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import io.jsonwebtoken.JwtException;
import it.aman.authenticationservice.annotation.Loggable;
import it.aman.authenticationservice.config.exception.AuthException;
import it.aman.authenticationservice.config.exception.AuthExceptionEnums;
import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.dal.entity.AuthUser;
import it.aman.authenticationservice.dal.repository.UserRepository;
import it.aman.authenticationservice.service.security.JwtTokenUtil;
import it.aman.authenticationservice.service.security.UserPrincipal;
import it.aman.authenticationservice.util.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationServiceImpl {
    
    private final AuthenticationManager authenticationManager;

    private final JwtTokenUtil jwtTokenUtil;

    private final UserDetailsService userDetailsService;
    
    private final UserRepository userRepository;
    
    private final ApiEndpointService endpointService;
    
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public String authenticate(String username, String password) throws AuthException {
        try {
            if(StringUtils.isAnyBlank(username, password)) {
                throw AuthExceptionEnums.AUTHENTICATION_DATA_REQUIRED.get();
            }
            
            UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
            Authentication authentication = authenticationManager.authenticate(upToken);
            
            // Note: Updating {@literal lastAccess} could be done in the UserDetailsService, 
            // the problem is it will always update it on every call, since the service is called on every call for validation of tokens.
            {
                AuthUser user = userRepository.findByAccountEmail(username).orElseThrow(AuthExceptionEnums.USER_NOT_FOUND);
                user.getAccount().setLastAccess(OffsetDateTime.now());
                userRepository.updateAndFlush(user);
            }
            
            return jwtTokenUtil.generateToken((UserPrincipal) authentication.getPrincipal());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } 
    }
    
    
    @Loggable
    @SuppressWarnings("unchecked")
    public boolean validateToken(HttpServletRequest httpServletRequest) throws Exception {
        final String authHeader = httpServletRequest.getHeader(AuthConstants.AUTH_HEADER_STRING);
        final String requestedUrl = httpServletRequest.getHeader(AuthConstants.X_REQUESTED_URL);
        final String requestedUrlHttpMethod = httpServletRequest.getHeader(AuthConstants.X_REQUESTED_URL_HTTP_METHOD);
        String username = null; 
        String authToken = null;
        if (StringUtils.isNoneBlank(authHeader, requestedUrl, requestedUrlHttpMethod) && authHeader.startsWith(AuthConstants.AUTH_TOKEN_PREFIX)) {
            authToken = authHeader.replace(AuthConstants.AUTH_TOKEN_PREFIX, "");
            try {
                username = (String) jwtTokenUtil.extractClaim(authToken, "sub");
            } catch (IllegalArgumentException e) {
                log.error("An error occured during getting username from token", e);
                throw e;
            } catch (JwtException e) {
                log.error("The token is expired and not valid anymore", e);
                throw e;
            }
        } else {
            log.warn("Couldn't find bearer string, will ignore the header");
        }
        
        if(StringUtils.isBlank(username)) return false;
        
        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        if(userDetails == null) return false;
        if(Boolean.FALSE.equals(jwtTokenUtil.verifyToken(authToken, userDetails))) return false;
        
        // validate url and corresponding permission
        List<String> permissions =  (ArrayList<String>) jwtTokenUtil.extractClaim(authToken, "permissions");
        List<AuthEndpoint> endpoints = endpointService.getData();
        PathMatcher matcher = new AntPathMatcher();
        for(AuthEndpoint ep : endpoints) {
            if(matcher.match(ep.getEndpoint(), requestedUrl) && ep.getHttpMethod().equalsIgnoreCase(requestedUrlHttpMethod)) {
                return permissions.contains(ep.getPermission());
            }
        }
        return false;
    }
}
