package privelink.backend.service;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import privelink.backend.dto.RegistroRequest;
import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;
import privelink.backend.model.Usuario;
import privelink.backend.repository.UsuarioRepository;

import java.util.Locale;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarCliente(RegistroRequest request) {

        if (request == null ||
                request.getNombre() == null ||
                request.getNombre().isBlank() ||
                request.getCorreo() == null ||
                request.getCorreo().isBlank() ||
                request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Datos de registro invalidos"
            );
        }

        String correo = request.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DuplicateKeyException(
                    "No se pudo completar el registro"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);

        usuario.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        usuario.setRol(RolUsuario.CLIENTE);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        return usuarioRepository.save(usuario);
    }
}