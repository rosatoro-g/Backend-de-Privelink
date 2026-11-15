
package privelink.backend.controller;

import jakarta.validation.Valid;

import org.springframework.dao.DuplicateKeyException;
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

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @PostMapping("/registro")
    public ResponseEntity<RegistroResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        Usuario usuario = registroService.registrarCliente(request);

        RegistroResponse response = new RegistroResponse();

        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setCorreo(usuario.getCorreo());
        response.setMensaje("Registro realizado correctamente");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
