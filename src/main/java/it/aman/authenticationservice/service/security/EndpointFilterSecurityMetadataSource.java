package it.aman.authenticationservice.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.common.util.ERPConstants;

@Component
public class EndpointFilterSecurityMetadataSource implements FilterInvocationSecurityMetadataSource  {

    private Map<String, ConfigAttribute> configAttributeMap = new HashMap<>();
    
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        String url = fi.getRequestUrl();
        String httpMod = fi.getRequest().getMethod();
        List<ConfigAttribute> attributes = new ArrayList<>();

        // Lookup your database (or other source) using this information and populate the
        // list of attributes
        
        PathMatcher pathMatcher = new AntPathMatcher();
        for(Map.Entry<String, ConfigAttribute> attr : configAttributeMap.entrySet()) {
            String[] patternAndMod = attr.getKey().split(ERPConstants.ATTRIBUTE_SEPARATOR);
            if (pathMatcher.match(patternAndMod[0], url) && httpMod.equalsIgnoreCase(patternAndMod[1])) {
                attributes.add(configAttributeMap.get(attr.getKey()));
            }
        }
        return attributes;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
    
    @SuppressWarnings("serial")
    public void setRequestMap(List<AuthEndpoint> endpoints) {
        Map<String, ConfigAttribute> attributeMap = new ConcurrentHashMap<>();
        for(AuthEndpoint ep : endpoints) {
            // there might be EP with same endpoint but different httpMod
            attributeMap.put(ep.getEndpoint() + ERPConstants.ATTRIBUTE_SEPARATOR + ep.getHttpMethod(), new ConfigAttribute() {
                
                @Override
                public String getAttribute() {
                   return ep.getPermission();
                }
            });
        }
        configAttributeMap = attributeMap;
    }
}
