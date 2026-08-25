package com.syscom.fep.gateway.configuration;

import com.syscom.fep.base.configurer.FEPConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // by default uses corsConfigurationSource
        http.cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                // disable Cross-Site Request Forgery
                .csrf(AbstractHttpConfigurer::disable)
                // .formLogin(AbstractHttpConfigurer::disable)
                // .httpBasic(AbstractHttpConfigurer::disable)
                // .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry.anyRequest().permitAll())
                // adding CSP Header using Spring Security
                .headers(httpSecurityHeadersConfigurer -> httpSecurityHeadersConfigurer
                        .contentSecurityPolicy(contentSecurityPolicyConfig -> contentSecurityPolicyConfig.policyDirectives("default-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                // 2024-10-28 Richard modified for 【Spring Permissive Content Security Policy】
                                // "img-src 'self' data: *; " +
                                // "media-src *; " +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval';" +
                                "style-src 'self' 'unsafe-inline';"))
                        // 2024-10-17 Richard add for 【Spring Missing HSTS Header】
                        .httpStrictTransportSecurity(
                                httpStrictTransportSecurityConfig -> httpStrictTransportSecurityConfig
                                        .includeSubDomains(true)
                                        .maxAgeInSeconds(31536000))
                        .xssProtection(Customizer.withDefaults())
                );
        return http.build();
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(FEPConfig.getInstance().getSecurityCorsAllowedOrigins());
        configuration.setAllowedMethods(Collections.singletonList("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
