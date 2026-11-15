
package privelink.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import privelink.backend.dto.LoginRequest;
import privelink.backend.dto.LoginResponse;
import privelink.backend.model.Usuario;
import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;
import privelink.backend.repository.UsuarioRepository;
import privelink.backend.security.JwtService;

import java.util.Locale;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(
            PasswordEncoder passwordEncoder,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {

        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    public boolean verificarPassword(
            String passwordIngresada,
            String passwordGuardada) {

        if (passwordIngresada == null ||
                passwordGuardada == null) {
            return false;
        }

        return passwordEncoder.matches(
                passwordIngresada,
                passwordGuardada
        );
    }

    public boolean usuarioEstaActivo(Usuario usuario) {

        return usuario != null
                && usuario.getEstado() == EstadoUsuario.ACTIVO;
    }

    public boolean usuarioTieneRol(
            Usuario usuario,
            RolUsuario rolRequerido) {

        return usuario != null
                && rolRequerido != null
                && usuario.getRol() == rolRequerido;
    }

    public LoginResponse iniciarSesion(LoginRequest request) {

        if (request == null ||
                request.getCorreo() == null ||
                request.getCorreo().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new BadCredentialsException(
                    "Credenciales invalidas"
            );
        }

        String correo = request.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Credenciales invalidas"
                        )
                );

        if (!verificarPassword(
                request.getPassword(),
                usuario.getPassword())) {

            throw new BadCredentialsException(
                    "Credenciales invalidas"
            );
        }

        if (!usuarioEstaActivo(usuario) ||
                usuario.getRol() == null) {

            throw new BadCredentialsException(
                    "Credenciales invalidas"
            );
        }

        String token = jwtService.generarToken(usuario);

        LoginResponse response = new LoginResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setCorreo(usuario.getCorreo());
        response.setRol(usuario.getRol());
        response.setToken(token);

        return response;
    }
}
