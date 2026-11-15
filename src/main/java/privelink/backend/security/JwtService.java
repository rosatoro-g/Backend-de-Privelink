
package privelink.backend.security;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import privelink.backend.model.Usuario;
import privelink.backend.model.EstadoUsuario;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generarToken(Usuario usuario) {

        if (usuario == null
                || usuario.getId() == null
                || usuario.getId().isBlank()
                || usuario.getRol() == null
                || usuario.getEstado() != EstadoUsuario.ACTIVO) {

            throw new IllegalArgumentException(
                    "Usuario no valido para generar token"
            );
        }

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://auth.privelink.cl")
                .subject(usuario.getId())
                .audience(java.util.List.of("privelink-api"))
                .issuedAt(ahora)
                .notBefore(ahora)
                .expiresAt(ahora.plus(15, ChronoUnit.MINUTES))
                .id(UUID.randomUUID().toString())
                .claim("rol", usuario.getRol().name())
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
