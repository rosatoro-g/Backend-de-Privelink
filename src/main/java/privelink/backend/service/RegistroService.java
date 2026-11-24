package privelink.backend.service;

import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import privelink.backend.dto.RegistroRequest;
import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;
import privelink.backend.model.Usuario;
import privelink.backend.repository.UsuarioRepository;

@Service
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistroService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =====================================================
    // REGISTRO CLIENTE
    // =====================================================

    public Usuario registrarCliente(RegistroRequest request) {
        return registrarUsuario(
                request,
                RolUsuario.CLIENTE
        );
    }

    // =====================================================
    // REGISTRO COLABORADOR
    // =====================================================

    public Usuario registrarColaborador(RegistroRequest request) {
        return registrarUsuario(
                request,
                RolUsuario.COLABORADOR
        );
    }

    // =====================================================
    // LÓGICA COMÚN DE REGISTRO
    // =====================================================

    private Usuario registrarUsuario(
            RegistroRequest request,
            RolUsuario rol
    ) {

        validarRequest(request);

        String correo = request.getCorreo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new DuplicateKeyException(
                    "No se pudo completar el registro"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(
                request.getNombre().trim()
        );

        usuario.setCorreo(correo);

        usuario.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        usuario.setRol(rol);

        usuario.setEstado(
                EstadoUsuario.ACTIVO
        );

        return usuarioRepository.save(usuario);
    }

    // =====================================================
    // VALIDACIÓN
    // =====================================================

    private void validarRequest(
            RegistroRequest request
    ) {

        if (
            request == null ||
            request.getNombre() == null ||
            request.getNombre().isBlank() ||
            request.getCorreo() == null ||
            request.getCorreo().isBlank() ||
            request.getPassword() == null ||
            request.getPassword().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Datos de registro invalidos"
            );
        }
    }
}