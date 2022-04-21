package com.andersenlab.benefits.security;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Profile({ "!test" })
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
                .antMatchers(HttpMethod.PATCH, "/roles").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/roles/**").hasRole("ADMIN")
                // users
                .antMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/users/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/users").hasRole("USER")
                .antMatchers(HttpMethod.PATCH, "/users").hasRole("USER")
                .antMatchers(HttpMethod.DELETE, "/users/**").hasRole("USER")
                // discounts
                .antMatchers(HttpMethod.GET, "/discounts").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/discounts/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/discounts").hasAnyRole("MODERATOR")
                .antMatchers(HttpMethod.PATCH, "/discounts/**").hasAnyRole("MODERATOR")
                .antMatchers(HttpMethod.DELETE, "/discounts/**").hasAnyRole("MODERATOR")

                // categories
                .antMatchers(HttpMethod.GET, "/categories").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/categories/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/categories").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/categories/**").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/categories/**").hasAnyRole("ADMIN")
                // locations
                .antMatchers(HttpMethod.GET, "/locations").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/locations/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/locations").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/locations/**").hasAnyRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/locations/**").hasAnyRole("ADMIN")
                // companies
                .antMatchers(HttpMethod.GET, "/companies").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.GET, "/companies/**").hasAnyRole("ADMIN", "MODERATOR", "USER")
                .antMatchers(HttpMethod.POST, "/companies").hasAnyRole("MODERATOR")
                .antMatchers(HttpMethod.PATCH, "/companies/**").hasAnyRole("MODERATOR")
                .antMatchers(HttpMethod.DELETE, "/companies/**").hasAnyRole("MODERATOR")

                // swagger
                .antMatchers(AUTH_WHITELIST).permitAll()
                .anyRequest().authenticated()
                .and().httpBasic();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.cors().and().csrf().disable();
    }
}
