package it.aman.authenticationservice.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import io.jsonwebtoken.JwtException;
import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.dal.entity.AuthTokenStorage;
import it.aman.authenticationservice.dal.entity.AuthUser;
import it.aman.authenticationservice.dal.repository.UserRepository;
import it.aman.authenticationservice.service.security.JwtTokenUtil;
import it.aman.authenticationservice.service.security.UserPrincipal;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;
import it.aman.common.util.ERPConstants;
import it.aman.common.util.GeneralUtils;
import it.aman.common.util.StringUtils;
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
    
    private final TokenStorageService tokenStorageService;
    
    private final HttpServletResponse httpServletResponse;
    
    private static final PathMatcher matcher = new AntPathMatcher();
    
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
                userRepository.update(user);
            }
            
            Map<String, String> tokenMap = jwtTokenUtil.generateToken((UserPrincipal) authentication.getPrincipal(), false);
            AuthTokenStorage storage = tokenStorageService.save(tokenMap);
            
            // enhance response with tokens
            addCookieToResponse(ERPConstants.TOKEN, storage.getToken(),  ERPConstants.AUTH_TOKEN_VALIDITY / 1000); // adapt to FE date format/length
            addCookieToResponse(ERPConstants.REFRESH_TOKEN, storage.getRefreshToken(),  -1);
            
            return tokenMap.getOrDefault(ERPConstants.TOKEN, "");
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
            final String requestedUrl = httpServletRequest.getHeader(ERPConstants.X_REQUESTED_URL);
            final String subject = httpServletRequest.getHeader(ERPConstants.X_REQUEST_URL_SUBJECT);
            final String authHeader = httpServletRequest.getHeader(ERPConstants.AUTH_HEADER_STRING);
            final String requestedUrlHttpMethod = httpServletRequest.getHeader(ERPConstants.X_REQUESTED_URL_HTTP_METHOD);
            String authToken = null;
            
            log.info("Validating token: {}", authHeader);
            
            List<AuthEndpoint> endpoints = endpointService.getData();
            // for public api's we don't have to have them in the endpoint table
            if (StringUtils.isBlank(authToken)) {
                checkIfPublic(requestedUrl, requestedUrlHttpMethod, endpoints);
            }
            
            if (StringUtils.isNoneBlank(requestedUrl, requestedUrlHttpMethod) && authHeader.startsWith(ERPConstants.AUTH_TOKEN_PREFIX)) {
                authToken = authHeader.replace(ERPConstants.AUTH_TOKEN_PREFIX, "");
                username = (String) jwtTokenUtil.extractClaim(authToken, ERPConstants.SUBJECT);
            } else {
                log.warn("Couldn't find bearer/requestedUlr/httpmethod.");
            }

            if (StringUtils.isBlank(username) || !it.aman.common.util.StringUtils.equals(subject, username)) {
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

    /**
     * Refresh a token once expired.
     * 
     * This method should be called with an interceptor from the front facing client
     * checking the return value of {@code validateToken()}
     * 
     * @param httpServletRequest
     * @throws ERPException
     */
    @Loggable
    @Transactional(rollbackFor = Exception.class)
    public void refresh(HttpServletRequest httpServletRequest) throws ERPException {
        try {
            final String subject = httpServletRequest.getHeader(ERPConstants.X_REQUEST_URL_SUBJECT);
            // should be fetched from cookie
            final String refreshToken = getRefreshToken(httpServletRequest);
            
            if(StringUtils.isBlank(refreshToken)) {
                throw ERPExceptionEnums.INVALID_FIELD_VALUE_EXCEPTION.get().setErrorMessage("Refresh token can't be empty.");
            }
            
            if (StringUtils.isBlank(subject)) {
                log.error("User name not found in the request.");
                throw ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get();
            }
            
            AuthTokenStorage storage = tokenStorageService.findByRefreshToken(refreshToken);
            if(storage == null) {
                throw ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get();
            }
            
            if (!StringUtils.equals(subject, storage.getOwner())) {
                log.error("User name different from token subject. Username: {}, subject: {}", storage.getOwner(), subject);
                throw ERPExceptionEnums.UNAUTHORIZED_EXCEPTION.get();
            }
            UserDetails principal = userDetailsService.loadUserByUsername(storage.getOwner());
            if(!principal.isEnabled() || !principal.isAccountNonExpired() || !principal.isAccountNonLocked()) {
                log.error("Can not update token, account not active.");
                return;
            }
            
            Map<String, String> tokenMap = jwtTokenUtil.generateToken((UserPrincipal) principal, true);
            storage.setToken(tokenMap.get(ERPConstants.TOKEN));
            storage.setRenewCount(storage.getRenewCount() + 1);
            tokenStorageService.update(storage);
            
            // enhance response with tokens
            String token = tokenMap.getOrDefault(ERPConstants.TOKEN, "");
            addCookieToResponse(ERPConstants.TOKEN, token,  ERPConstants.AUTH_TOKEN_VALIDITY / 1000); // adapt to FE date format/length
            addCookieToResponse(ERPConstants.REFRESH_TOKEN, refreshToken,  -1);
        } catch (ERPException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        } 
    }
    
    
    // *********
    private String checkIfPublic(String requestedUrl, String requestedUrlHttpMethod, List<AuthEndpoint> endpoints) throws ERPException {
        for (AuthEndpoint ep : endpoints) {
            if (serviceAndUrlMatches(ep, GeneralUtils.parseServiceNameAndUrl(requestedUrl), requestedUrlHttpMethod, matcher)) {
                return ""; // Unauthorized
            }
        }
        return ERPConstants.ANONYMOUS_USER;
    }
    
    @SuppressWarnings("unchecked")
    private boolean validatePermission(String username, String authToken, String requestedUrl, String requestedUrlHttpMethod, List<AuthEndpoint> endpoints) throws ERPException {
        UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);
        if(userDetails == null) {
            log.info("Corresponding userDetail data not found.");
            throw ERPExceptionEnums.ACCOUNT_NOT_FOUND.get();
        }
        
        if(Boolean.FALSE.equals(jwtTokenUtil.verifyToken(authToken, userDetails))) {
            log.info("Token verification not successful.");
            return false;
        }
        
        // validate url with corresponding permission
        List<String> permissions =  (ArrayList<String>) jwtTokenUtil.extractClaim(authToken, ERPConstants.PERMISSIONS);
        for(AuthEndpoint ep : endpoints) {
            if(serviceAndUrlMatches(ep, GeneralUtils.parseServiceNameAndUrl(requestedUrl), requestedUrlHttpMethod, matcher)) {
                return permissions.contains(ep.getPermission());
            }
        }
        return false;
    }
    
    private boolean serviceAndUrlMatches(final AuthEndpoint ep, final String[] serviceNameUrl, final String requestedUrlHttpMethod, final PathMatcher matcher) {
        return ep.getId().getServiceName().equals(serviceNameUrl[0]) 
                && matcher.match(ep.getId().getEndpoint(), serviceNameUrl[1]) 
                && ep.getId().getHttpMethod().equalsIgnoreCase(requestedUrlHttpMethod);
    }
    
    private String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        for(Cookie c : cookies) {
            if(ERPConstants.REFRESH_TOKEN.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * http-only cookie, it is a very bad practice to save tokens in front-facing
     * clients where the token is accessible by a script(like js etc)
     * 
     * @param name
     * @param value
     * @param age
     */
    private void addCookieToResponse(final String name, final String value, int age) {
        Cookie cookieToken = new Cookie(name, value);
        cookieToken.setHttpOnly(true);
        cookieToken.setMaxAge(age);
        cookieToken.setPath("/");

        httpServletResponse.addCookie(cookieToken);
    }
}
