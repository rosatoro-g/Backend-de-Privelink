package privelink.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Endpoint público
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/estado"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/registro"
                        ).permitAll()

                        // Swagger / OpenAPI público
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Rutas administrativas
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Rutas para colaboradores
                        .requestMatchers("/api/colaborador/**")
                        .hasAnyRole("COLABORADOR", "ADMIN")

                        // Rutas para clientes
                        .requestMatchers("/api/cliente/**")
                        .hasRole("CLIENTE")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/servicios/**"
                        ).authenticated()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/servicios/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/servicios/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/servicios/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }
    private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {

        return jwt -> {

            String rol = jwt.getClaimAsString("rol");

            if (rol == null || !List.of(
                    "CLIENTE", "COLABORADOR", "ADMIN"
            ).contains(rol)) {
                throw new org.springframework.security.oauth2.jwt.BadJwtException(
                        "Rol JWT invalido"
                );
            }

            return new UsernamePasswordAuthenticationToken(
                    jwt,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol))
            );
        };
    }


}