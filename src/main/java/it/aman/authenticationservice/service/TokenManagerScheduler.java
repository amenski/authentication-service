package it.aman.authenticationservice.service;

import java.util.Date;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.dal.entity.AuthTokenStorage;
import it.aman.authenticationservice.dal.repository.TokenStorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class TokenManagerScheduler {

    private final TokenStorageRepository tokenStorageRepository;
    
    @Scheduled(fixedDelay = 6000)
    public void run() {
        List<AuthTokenStorage> all = tokenStorageRepository.findAll();
        for(AuthTokenStorage str : all) {
            if(new Date().getTime() > str.getExpiration()) {
                log.info("Removing token: {}", str.getTokenString());
                tokenStorageRepository.deleteById(str.getId());
            }
        }
    }
}
