package it.aman.authenticationservice.service;

import java.util.Date;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.dal.entity.AuthTokenStorage;
import it.aman.authenticationservice.dal.repository.TokenStorageRepository;
import it.aman.authenticationservice.service.security.JwtTokenUtil;
import it.aman.common.annotation.Loggable;
import it.aman.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ideally this is a good way to check if incoming token is tampered(comparing to the original one)
 * 
 * @author Aman
 *
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TokenStorageService {

    private final TokenStorageRepository storageRepository;
    
    private final JwtTokenUtil tokenUtil;
    
    @Loggable
    public boolean test(final String token) {
        if(StringUtils.isBlank(token)) {
            log.error("Token not found.");
            return false;
        }
        return storageRepository.findByTokenString(token).orElse(null) != null;
    }
    
    @Loggable
    public void save(final String token) {
        Date exp = (Date) tokenUtil.extractClaim(token, "exp");
        String subject = (String) tokenUtil.extractClaim(token, "sub");
        if(exp == null || StringUtils.isBlank(subject)) {
            log.error("Unable to save token: {}", token);
            return;
        }
        AuthTokenStorage entity = AuthTokenStorage.builder().tokenString(token).expiration(exp.getTime()).owner(subject).build();
        storageRepository.save(entity);
    }
}
