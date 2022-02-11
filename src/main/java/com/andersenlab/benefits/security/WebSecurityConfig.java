package com.andersenlab.benefits.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
public class WebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {
    private static final String[] AUTH_WHITELIST = {
            "/authenticate",
            "/swagger-resources/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/v3/api-docs",
            "/webjars/**"
    };

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                // roles
                .antMatchers(HttpMethod.GET, "/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/roles/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/roles/**").hasRole("ADMIN")
                // users
                .antMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/users").hasAnyRole("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.PUT, "/users").hasAnyRole("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.DELETE, "/users/**").hasAnyRole("ADMIN", "MODERATOR")
                // discounts
                .antMatchers(HttpMethod.GET, "/discounts").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/discount/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/discount").hasAnyRole("MODERATOR")
                .antMatchers(HttpMethod.PUT, "/discount/**").hasAnyRole("ADMIN", "MODERATOR")
                .antMatchers(HttpMethod.DELETE, "/discount/**").hasAnyRole("ADMIN", "MODERATOR")
                // swagger
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and().httpBasic();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors().and().csrf().disable();
    }
}
