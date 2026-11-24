package privelink.backend.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import privelink.backend.model.Servicio;
import privelink.backend.service.ServicioService;

@RestController
@RequestMapping("/api/servicios")
public class ServicioController {

    private final ServicioService servicioService;

    public ServicioController(
            ServicioService servicioService
    ) {
        this.servicioService = servicioService;
    }

    // =========================
    // GET TODOS / POR PERFIL
    // =========================

    @GetMapping
    public ResponseEntity<List<Servicio>>
            obtenerServicios(
                    @RequestParam(
                        required = false
                    )
                    String perfilId
            ) {

        if (
            perfilId != null &&
            !perfilId.isBlank()
        ) {

            return ResponseEntity.ok(
                    servicioService
                            .obtenerPorPerfil(
                                    perfilId
                            )
            );
        }

        return ResponseEntity.ok(
                servicioService.obtenerTodos()
        );
    }

    // =========================
    // GET POR ID
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<Servicio>
            obtenerPorId(
                    @PathVariable String id
            ) {

        return ResponseEntity.ok(
                servicioService.obtenerPorId(id)
        );
    }

    // =========================
    // CREATE
    // =========================

    @PostMapping
    public ResponseEntity<Servicio>
            crear(
                    @RequestBody
                    Servicio servicio,

                    @AuthenticationPrincipal
                    Jwt jwt
            ) {

        String usuarioId =
                jwt.getSubject();

        Servicio creado =
                servicioService.crear(
                        servicio,
                        usuarioId
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/servicios/"
                                + creado.getId()
                        )
                )
                .body(creado);
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    public ResponseEntity<Servicio>
            actualizar(
                    @PathVariable
                    String id,

                    @RequestBody
                    Servicio servicio,

                    @AuthenticationPrincipal
                    Jwt jwt
            ) {

        String usuarioId =
                jwt.getSubject();

        boolean admin =
                esAdmin(jwt);

        return ResponseEntity.ok(
                servicioService.actualizar(
                        id,
                        servicio,
                        usuarioId,
                        admin
                )
        );
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
            eliminar(
                    @PathVariable
                    String id,

                    @AuthenticationPrincipal
                    Jwt jwt
            ) {

        String usuarioId =
                jwt.getSubject();

        boolean admin =
                esAdmin(jwt);

        servicioService.eliminar(
                id,
                usuarioId,
                admin
        );

        return ResponseEntity.ok(
                Map.of(
                        "mensaje",
                        "Servicio eliminado correctamente"
                )
        );
    }

    // =========================
    // UTILIDAD DE ROL
    // =========================

    private boolean esAdmin(Jwt jwt) {

        String rol =
                jwt.getClaimAsString("rol");

        return "ADMIN".equals(rol);
    }
}