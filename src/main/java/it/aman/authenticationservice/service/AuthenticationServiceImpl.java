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
import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.dal.entity.AuthUser;
import it.aman.authenticationservice.dal.repository.UserRepository;
import it.aman.authenticationservice.service.security.JwtTokenUtil;
import it.aman.authenticationservice.service.security.UserPrincipal;
import it.aman.common.ERPConstants;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;
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
    
    @Loggable(exclusions = {"password"})
    @Transactional(rollbackFor = Exception.class)
    public String authenticate(String username, String password) throws ERPException {
        try {
            if(StringUtils.isAnyBlank(username, password)) {
                throw ERPExceptionEnums.AUTHENTICATION_DATA_REQUIRED.get();
            }
            
            UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password, new ArrayList<>());
            Authentication authentication = authenticationManager.authenticate(upToken);
            
            // Note: Updating {@literal lastAccess} could be done in the UserDetailsService, 
            // the problem is it will always update it on every call, since the service is called on every call for validation of tokens.
            {
                AuthUser user = userRepository.findByAccountEmail(username).orElseThrow(ERPExceptionEnums.USER_NOT_FOUND);
                user.getAccount().setLastAccess(OffsetDateTime.now());
                userRepository.updateAndFlush(user);
            }
            
            return jwtTokenUtil.generateToken((UserPrincipal) authentication.getPrincipal());
        } catch (ERPException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } 
    }
    
    @Loggable
    public String validateToken(HttpServletRequest httpServletRequest) throws Exception {
        boolean success = false;
        String username = null;
        try {
            final String authHeader = httpServletRequest.getHeader(ERPConstants.AUTH_HEADER_STRING);
            final String requestedUrl = httpServletRequest.getHeader(ERPConstants.X_REQUESTED_URL);
            final String requestedUrlHttpMethod = httpServletRequest.getHeader(ERPConstants.X_REQUESTED_URL_HTTP_METHOD);
            final String subject = httpServletRequest.getHeader(ERPConstants.X_REQUESTED_URL_SUBJECT);
            String authToken = null;

            log.info("Validating token: {}", authHeader);
            
            List<AuthEndpoint> endpoints = endpointService.getData();
            // for public api's we don't have to have them in the endpoint table
            if (StringUtils.isBlank(authHeader)) {
                PathMatcher matcher = new AntPathMatcher();
                for (AuthEndpoint ep : endpoints) {
                    if (matcher.match(ep.getEndpoint(), requestedUrl) && ep.getHttpMethod().equalsIgnoreCase(requestedUrlHttpMethod)) {
                        return "";
                    }
                }
                return ERPConstants.ANONYMOUS_USER;
            }

            if (StringUtils.isNoneBlank(requestedUrl, requestedUrlHttpMethod) && authHeader.startsWith(ERPConstants.AUTH_TOKEN_PREFIX)) {
                authToken = authHeader.replace(ERPConstants.AUTH_TOKEN_PREFIX, "");
                username = (String) jwtTokenUtil.extractClaim(authToken, "sub");
            } else {
                log.warn("Couldn't find bearer/requestedUlr/httpmethod.");
            }

            if (StringUtils.isBlank(username) || !it.aman.common.StringUtils.equals(subject, username)) {
                log.error("User name not found or is different than token subject. Username: {}, subject: {}", username, subject);
                throw ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get();
            }

            success = validatePermission(username, authToken, requestedUrl, requestedUrlHttpMethod, endpoints);
            log.info("Token validation result: {}", success ? "Authorized" : "Unauthorized");
            return success ? username : "";
        } catch (IllegalArgumentException e) {
            log.error("An error occured during getting username from token", e);
            throw e;
        } catch (JwtException e) {
            log.error("The token is expired and not valid anymore", e);
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean validatePermission(String username, String authToken, String requestedUrl, String requestedUrlHttpMethod, List<AuthEndpoint> endpoints) {
        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        if(userDetails == null) {
            log.info("Corresponding userDetail data not found.");
            return false;
        }
        
        if(Boolean.FALSE.equals(jwtTokenUtil.verifyToken(authToken, userDetails))) {
            log.info("Token verification not successful.");
            return false;
        }
        
        // validate url with corresponding permission
        List<String> permissions =  (ArrayList<String>) jwtTokenUtil.extractClaim(authToken, "permissions");
        PathMatcher matcher = new AntPathMatcher();
        for(AuthEndpoint ep : endpoints) {
            if(matcher.match(ep.getEndpoint(), requestedUrl) && ep.getHttpMethod().equalsIgnoreCase(requestedUrlHttpMethod)) {
                return permissions.contains(ep.getPermission());
            }
        }
        return false;
    }
}
