package privelink.backend.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import privelink.backend.dto.RegistroRequest;
import privelink.backend.dto.RegistroResponse;
import privelink.backend.model.Usuario;
import privelink.backend.service.RegistroService;

@RestController
@RequestMapping("/api/auth")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(
            RegistroService registroService
    ) {
        this.registroService = registroService;
    }

    // =====================================================
    // REGISTRO DE CLIENTE
    // =====================================================

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrarCliente(
            @Valid @RequestBody RegistroRequest request
    ) {

        Usuario usuario =
                registroService.registrarCliente(request);

        RegistroResponse response =
                crearRespuesta(
                        usuario,
                        "Registro realizado correctamente"
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // REGISTRO DE COLABORADOR
    // =====================================================

    @PostMapping("/registro-colaborador")
    public ResponseEntity<RegistroResponse> registrarColaborador(
            @Valid @RequestBody RegistroRequest request
    ) {

        Usuario usuario =
                registroService.registrarColaborador(request);

        RegistroResponse response =
                crearRespuesta(
                        usuario,
                        "Colaborador registrado correctamente"
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // =====================================================
    // CREAR RESPUESTA
    // =====================================================

    private RegistroResponse crearRespuesta(
            Usuario usuario,
            String mensaje
    ) {

        RegistroResponse response =
                new RegistroResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setCorreo(usuario.getCorreo());
        response.setMensaje(mensaje);

        return response;
    }
}