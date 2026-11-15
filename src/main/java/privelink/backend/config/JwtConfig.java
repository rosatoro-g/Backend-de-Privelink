
package privelink.backend.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;



@Configuration
public class JwtConfig {

    @Value("${JWT_KEYSTORE_PATH}")
    private String keystorePath;

    @Value("${JWT_KEYSTORE_PASSWORD}")
    private String keystorePassword;

    @Value("${JWT_KEY_ALIAS}")
    private String keyAlias;

    @Bean
    public RSAKey rsaKey() throws Exception {

        KeyStore keyStore =
                KeyStore.getInstance("PKCS12");

        char[] password = keystorePassword.toCharArray();

        try {
            try (FileInputStream input =
                         new FileInputStream(keystorePath)) {

                keyStore.load(input, password);
            }

            PrivateKey privateKey = (PrivateKey)
                    keyStore.getKey(keyAlias, password);

            Certificate certificate =
                    keyStore.getCertificate(keyAlias);

            if (privateKey == null || certificate == null) {
                throw new IllegalStateException(
                        "No se encontraron las claves JWT"
                );
            }

            RSAPublicKey publicKey = (RSAPublicKey)
                    certificate.getPublicKey();

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(keyAlias)
                    .build();

        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(jwkSet)
        );
    }
    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {

        RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(
                        "https://auth.privelink.cl"
                );

        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (jwt.getAudience().contains("privelink-api")) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "Audiencia JWT no permitida",
                            null
                    )
            );
        };

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                )
        );

        return decoder;
    }
}
