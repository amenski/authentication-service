package it.aman.authenticationservice.service.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import it.aman.authenticationservice.dal.entity.AuthEndpoint;
import it.aman.authenticationservice.service.ApiEndpointService;
import it.aman.authenticationservice.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;

/**
 *
 * <br/>
 * Note: Web and Method security might be used together specially when we have
 * different roles/rights specific to some functionalities only. e.g. assume
 * that a user is able to edit, but not delete an object/entity. Unless method
 * security({@code @PreAuthorize} and the like) is specified, it will be
 * possible to do both.
 *
 *
 * @see {@link https://spring.io/guides/topicals/spring-security-architecture}
 * @author Aman
 *
 */

@Configuration
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AuthAuthenticationEntryPoint ethAuthenticationEntryPoint;

    private final UserDetailsServiceImpl ethUserDetailsService;

    private final JwtCustomRequestAuthenticationFilter jwtCustomRequestAuthenticationFilter;
    
    // endpoint related
    private final ApiEndpointService apiEndpointService;
    private final EndpointFilterSecurityMetadataSource ethEndpointFilterSecurityMetadataSource;
    
    @Resource
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    // http://blog.florian-hopf.de/2017/08/spring-security.html
    // configure(WebSecurity web) ... can be added to ignore checking on some
    // resources like files  and then `web.ignoring().antMatchers("/resources/**");`
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // authorizeRequests()'s order is important
        http.csrf().disable().authorizeRequests().antMatchers("/**").permitAll()
        .and()
				.addFilterBefore(jwtCustomRequestAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(dynamicallyUrlInterceptor(), FilterSecurityInterceptor.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(new CustomAccessDeniedHandler()) // AccessDeniedHandler only applies to authenticated users
                .authenticationEntryPoint(ethAuthenticationEntryPoint); // AuthenticationEntryPoint has to be the last, is invoked when an unauthenticated user attempts to access a protected resource
    }

    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(ethUserDetailsService);
        provider.setPasswordEncoder(encoder());

        return provider;
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    @DependsOn("apiEndpointService")
    public DynamicUrlInterceptor dynamicallyUrlInterceptor() {
        List<AuthEndpoint> endpoints = apiEndpointService.getData();
        ethEndpointFilterSecurityMetadataSource.setRequestMap(endpoints);
        DynamicUrlInterceptor interceptor = new DynamicUrlInterceptor();
        interceptor.setSecurityMetadataSource(ethEndpointFilterSecurityMetadataSource);

        List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList<>();
        decisionVoters.add(new AuthRoleVoter());

        interceptor.setAccessDecisionManager(new AuthAccessDecisionManager(decisionVoters));
        return interceptor;
    }
    
    public class CustomAccessDeniedHandler implements AccessDeniedHandler {
        private final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
        
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                log.warn("User: {} attempted to access the URL: {} ",  authentication.getName(), request.getRequestURI());
            }
            // resolve spring-security filter exceptions to @ControllerAdvice, AccessDeniedException in this case
            exceptionResolver.resolveException(request, response, null, accessDeniedException);
        }
        
    }
}
