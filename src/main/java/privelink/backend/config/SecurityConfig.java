package privelink.backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder
    ) throws Exception {

        http
                // CORS
                .cors(Customizer.withDefaults())

                // API REST con JWT
                .csrf(csrf -> csrf.disable())

                // Sin sesiones de servidor
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // CORS PREFLIGHT
                        // =========================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // =========================
                        // AUTENTICACIÓN PÚBLICA
                        // =========================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/estado"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro-colaborador"
                        ).permitAll()

                        // =========================
                        // SWAGGER / OPENAPI
                        // =========================
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =========================
                        // ADMIN
                        // =========================
                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")

                        // =========================
                        // COLABORADOR
                        // =========================
                        .requestMatchers(
                                "/api/colaborador/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        // =========================
                        // CLIENTE
                        // =========================
                        .requestMatchers(
                                "/api/cliente/**"
                        ).hasRole("CLIENTE")

                        // =========================
                        // SERVICIOS - LECTURA
                        // =========================
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/servicios/**"
                        ).authenticated()

                        // =========================
                        // SERVICIOS - CREAR
                        // =========================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/servicios/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        // =========================
                        // SERVICIOS - EDITAR
                        // =========================
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/servicios/**"
                        ).hasAnyRole(
                                "COLABORADOR",
                                "ADMIN"
                        )

                        // =========================
                        // SERVICIOS - ELIMINAR
                        // =========================
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

                // =========================
                // JWT
                // =========================
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                );

        return http.build();
    }

    // =====================================================
    // CORS
    // =====================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept"
                )
        );

        configuration.setExposedHeaders(
                List.of(
                        "Authorization",
                        "Location"
                )
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    // =====================================================
    // CLAIM "rol" -> ROLE_...
    // =====================================================

    private Converter<Jwt, AbstractAuthenticationToken>
            jwtAuthenticationConverter() {

        return jwt -> {

            String rol =
                    jwt.getClaimAsString("rol");

            if (
                rol == null ||
                !List.of(
                        "CLIENTE",
                        "COLABORADOR",
                        "ADMIN"
                ).contains(rol)
            ) {
                throw new org.springframework.security.oauth2.jwt.BadJwtException(
                        "Rol JWT invalido"
                );
            }

            return new UsernamePasswordAuthenticationToken(
                    jwt,
                    null,
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + rol
                            )
                    )
            );
        };
    }
}