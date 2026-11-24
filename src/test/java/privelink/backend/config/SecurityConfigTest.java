package privelink.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import privelink.backend.model.EstadoUsuario;
import privelink.backend.model.RolUsuario;
import privelink.backend.model.Usuario;
import privelink.backend.security.JwtService;
import privelink.backend.security.JwtTestHelper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void configurarJwtDecoder() {

        reset(jwtDecoder);

        when(jwtDecoder.decode(anyString()))
                .thenThrow(
                        new BadJwtException(
                                "Token JWT invalido"
                        )
                );
    }

    @Test
    void usuarioSinTokenNoPuedeAccederARutaAdmin()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/usuarios")
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void clienteNoPuedeAccederARutaAdmin()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/usuarios")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENTE"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void colaboradorNoPuedeAccederARutaAdmin()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/usuarios")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_COLABORADOR"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void clienteNoPuedeAccederARutaColaborador()
            throws Exception {

        mockMvc.perform(
                get("/api/colaborador/perfil")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENTE"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isForbidden()
        );
    }

    @Test
    void adminPuedeAccederARutaAdmin()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/prueba-acceso")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_ADMIN"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void colaboradorPuedeAccederARutaColaborador()
            throws Exception {

        mockMvc.perform(
                get("/api/colaborador/prueba-acceso")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_COLABORADOR"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void clientePuedeAccederARutaCliente()
            throws Exception {

        mockMvc.perform(
                get("/api/cliente/prueba-acceso")
                        .with(
                                jwt().authorities(
                                        new SimpleGrantedAuthority(
                                                "ROLE_CLIENTE"
                                        )
                                )
                        )
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void tokenInvalidoDebeSerRechazado()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/verificar-seguridad")
                        .header(
                                "Authorization",
                                "Bearer token.invalido.falso"
                        )
        )
        .andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    void rutaPublicaDebeFuncionarSinToken()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/estado")
        )
        .andExpect(
                status().isOk()
        );
    }

    @Test
    void tokenRealFirmadoPermiteAccesoCliente()
            throws Exception {

        JwtTestHelper helper =
                new JwtTestHelper();

        JwtService jwtService =
                new JwtService(
                        helper.crearEncoder()
                );

        Usuario usuario =
                new Usuario();

        usuario.setId(
                "cliente-test-123"
        );

        usuario.setRol(
                RolUsuario.CLIENTE
        );

        usuario.setEstado(
                EstadoUsuario.ACTIVO
        );

        String token =
                jwtService.generarToken(
                        usuario
                );

        doAnswer(
                invocation ->
                        helper
                                .crearDecoder()
                                .decode(token)
        )
        .when(jwtDecoder)
        .decode(token);

        mockMvc.perform(
                get("/api/cliente/prueba-acceso")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(
                status().isOk()
        );
    }
}