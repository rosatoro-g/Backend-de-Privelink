
package privelink.backend.security;

import com.nimbusds.jose.jwk.JWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import privelink.backend.config.JwtConfig;
import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;
import privelink.backend.model.Usuario;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jwt.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import java.time.temporal.ChronoUnit;

class JwtServiceTest {

    @Test
    void usuarioInactivoNoDebeGenerarToken() {

        JwtService jwtService = new JwtService(claims -> {
            fail("No debe intentar firmar un token");
            return null;
        });

        Usuario usuario = new Usuario();
        usuario.setId("123");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.INACTIVO);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(usuario)
        );
    }

    @Test
    void usuarioNuloNoDebeGenerarToken() {

        JwtService jwtService = new JwtService(claims -> {
            fail("No debe firmar un token");
            return null;
        });

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(null)
        );
    }

    @Test
    void usuarioSinRolNoDebeGenerarToken() {

        JwtService jwtService = new JwtService(claims -> {
            fail("No debe firmar un token");
            return null;
        });

        Usuario usuario = new Usuario();
        usuario.setId("123");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(usuario)
        );
    }

    @Test
    void usuarioSinIdNoDebeGenerarToken() {

        JwtService jwtService = new JwtService(claims -> {
            fail("No debe firmar un token");
            return null;
        });

        Usuario usuario = new Usuario();
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generarToken(usuario)
        );
    }

    @Test
    void tokenDebeFirmarseYValidarseConRSA() throws Exception {

        // Crear claves independientes para la prueba
        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(3072);

        KeyPair keyPair = generator.generateKeyPair();

        java.security.interfaces.RSAPublicKey publicKey =
                (java.security.interfaces.RSAPublicKey)
                        keyPair.getPublic();

        java.security.interfaces.RSAPrivateKey privateKey =
                (java.security.interfaces.RSAPrivateKey)
                        keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("test-key")
                .build();

        JwtEncoder encoder = new NimbusJwtEncoder(
                new ImmutableJWKSet<>(
                        new JWKSet(rsaKey)
                )
        );

        JwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(
                        org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256
                )
                .build();

        JwtService jwtService = new JwtService(encoder);

        Usuario usuario = new Usuario();
        usuario.setId("usuario-test-123");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        // Generar un token firmado
        String token = jwtService.generarToken(usuario);

        // Verificar la firma y leer su contenido
        Jwt jwt = decoder.decode(token);

        assertEquals("usuario-test-123", jwt.getSubject());
        assertEquals(
                "https://auth.privelink.cl",
                jwt.getIssuer().toString()
        );
        assertTrue(jwt.getAudience().contains("privelink-api"));
        assertEquals("CLIENTE", jwt.getClaimAsString("rol"));
        assertTrue(jwt.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void tokenModificadoDebeSerRechazado() throws Exception {

        JwtTestHelper helper = new JwtTestHelper();

        JwtService jwtService =
                new JwtService(helper.crearEncoder());

        Usuario usuario = new Usuario();
        usuario.setId("usuario-test-123");
        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        String tokenOriginal = jwtService.generarToken(usuario);

        String[] partes = tokenOriginal.split("\\.");

        // Alterar el contenido del token sin generar una nueva firma.
        String payloadAlterado = partes[1].charAt(0) == 'A'
                ? "B" + partes[1].substring(1)
                : "A" + partes[1].substring(1);

        String tokenModificado =
                partes[0] + "." +
                        payloadAlterado + "." +
                        partes[2];

        JwtDecoder decoder = helper.crearDecoder();

        assertThrows(
                JwtException.class,
                () -> decoder.decode(tokenModificado)
        );
    }

    @Test
    void tokenExpiradoDebeSerRechazado() throws Exception {

        JwtTestHelper helper = new JwtTestHelper();

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://auth.privelink.cl")
                .subject("usuario-test-123")
                .audience(java.util.List.of("privelink-api"))
                .issuedAt(ahora.minus(30, ChronoUnit.MINUTES))
                .expiresAt(ahora.minus(15, ChronoUnit.MINUTES))
                .claim("rol", "CLIENTE")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .build();

        String tokenExpirado = helper.crearEncoder()
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        JwtDecoder decoder = helper.crearDecoder();

        assertThrows(
                JwtException.class,
                () -> decoder.decode(tokenExpirado)
        );
    }
    @Test
    void tokenConAudienciaIncorrectaDebeSerRechazado() throws Exception {

        JwtTestHelper helper = new JwtTestHelper();

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://auth.privelink.cl")
                .subject("usuario-test-123")
                .audience(java.util.List.of("otra-aplicacion"))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(900))
                .claim("rol", "CLIENTE")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .build();

        String token = helper.crearEncoder()
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        assertThrows(
                JwtException.class,
                () -> helper.crearDecoder().decode(token)
        );
    }
    @Test
    void tokenConEmisorIncorrectoDebeSerRechazado() throws Exception {

        JwtTestHelper helper = new JwtTestHelper();

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://otro-sistema.example")
                .subject("usuario-test-123")
                .audience(java.util.List.of("privelink-api"))
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(900))
                .claim("rol", "CLIENTE")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .build();

        String token = helper.crearEncoder()
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        assertThrows(
                JwtException.class,
                () -> helper.crearDecoder().decode(token)
        );
    }
}
