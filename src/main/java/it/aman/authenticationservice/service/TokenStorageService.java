package it.aman.authenticationservice.service;

import java.util.Date;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.dal.entity.AuthTokenStorage;
import it.aman.authenticationservice.dal.repository.TokenStorageRepository;
import it.aman.authenticationservice.service.security.JwtTokenUtil;
import it.aman.common.annotation.Loggable;
import it.aman.common.exception.ERPException;
import it.aman.common.exception.ERPExceptionEnums;
import it.aman.common.util.ERPConstants;
import it.aman.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Ideally this is a good way to check if incoming token is tampered(compared to the original one)
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
    public boolean refreshTokenExists(final String refreshToken) {
        return findByRefreshToken(refreshToken) != null;
    }
    
    @Loggable
    public AuthTokenStorage findByRefreshToken(final String refreshToken) {
        if(StringUtils.isBlank(refreshToken)) {
            log.error("Token not found.");
            return null;
        }
        return storageRepository.findByRefreshToken(refreshToken).orElse(null);
    }
    
    @Loggable
    public AuthTokenStorage save(final Map<String, String> tokenMap) throws ERPException {
        final String token = tokenMap.getOrDefault(ERPConstants.TOKEN, "");
        final String refreshToken = tokenMap.getOrDefault(ERPConstants.REFRESH_TOKEN, "");
        if(StringUtils.isAnyBlank(token, refreshToken)) {
            log.error("Token not found.");
            throw ERPExceptionEnums.INVALID_FIELD_VALUE_EXCEPTION.get();
        }
        
        Date exp = (Date) tokenUtil.extractClaim(token, ERPConstants.EXPIRY);
        String subject = (String) tokenUtil.extractClaim(token, ERPConstants.SUBJECT);
        if(exp == null || StringUtils.isBlank(subject)) {
            log.error("Unable to save token: {}", tokenMap);
            throw ERPExceptionEnums.INVALID_FIELD_VALUE_EXCEPTION.get();
        }
        AuthTokenStorage entity = AuthTokenStorage.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiration(System.currentTimeMillis() + (1000 * 60 * 60 * 24)) // 24hr
                .owner(subject)
                .renewCount(0)
                .build();
        return storageRepository.save(entity);
    }
    
    @Loggable
    public void update(AuthTokenStorage entity) {
        storageRepository.save(entity);
    }
}
