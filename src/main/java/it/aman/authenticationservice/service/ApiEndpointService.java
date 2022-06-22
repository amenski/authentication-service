package it.aman.authenticationservice.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.dal.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiEndpointService {

    private final EndpointRepository endpointRepository;
    
    @Cacheable
    public List<AuthEndpoint> getData() {
        return endpointRepository.findAll();
    }
}
