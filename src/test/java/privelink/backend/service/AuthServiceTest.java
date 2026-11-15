package privelink.backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import privelink.backend.model.Usuario;
import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService =
            new AuthService(new BCryptPasswordEncoder());

    @Test
    void usuarioActivoDebeRetornarTrue() {

        Usuario usuario = new Usuario();
        usuario.setEstado(EstadoUsuario.ACTIVO);

        assertTrue(authService.usuarioEstaActivo(usuario));
    }

    @Test
    void usuarioInactivoDebeRetornarFalse() {

        Usuario usuario = new Usuario();
        usuario.setEstado(EstadoUsuario.INACTIVO);

        assertFalse(authService.usuarioEstaActivo(usuario));
    }

    @Test
    void usuarioClienteDebeTenerRolCliente() {

        Usuario usuario = new Usuario();
        usuario.setRol(RolUsuario.CLIENTE);

        assertTrue(authService.usuarioTieneRol(
                usuario, RolUsuario.CLIENTE
        ));

        assertFalse(authService.usuarioTieneRol(
                usuario, RolUsuario.ADMIN
        ));
    }

    @Test
    void passwordCorrectaDebeRetornarTrue() {

        String password = "Clave123!";
        String hash = new BCryptPasswordEncoder().encode(password);

        assertTrue(authService.verificarPassword(password, hash));

        assertFalse(authService.verificarPassword("Incorrecta", hash));
    }
}