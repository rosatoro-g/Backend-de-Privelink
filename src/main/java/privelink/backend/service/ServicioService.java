package privelink.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import privelink.backend.exception.RecursoNoEncontradoException;
import privelink.backend.model.Servicio;
import privelink.backend.repository.ServicioRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(
            ServicioRepository servicioRepository
    ) {
        this.servicioRepository = servicioRepository;
    }

    // =========================
    // READ
    // =========================

    public List<Servicio> obtenerTodos() {

        return servicioRepository.findAll();
    }

    public Servicio obtenerPorId(String id) {

        return servicioRepository
                .findById(id)
        .orElseThrow(
                        () -> new RecursoNoEncontradoException(
                                "Servicio no encontrado"
                        )
                );
        }

    public List<Servicio> obtenerPorPerfil(
            String perfilId
    ) {

        return servicioRepository
                .findByPerfilId(perfilId);
    }

    // =========================
    // CREATE
    // =========================

    public Servicio crear(
            Servicio servicio,
            String usuarioId
    ) {

        validar(servicio);

        if (
            usuarioId == null ||
            usuarioId.isBlank()
        ) {
            throw new AccessDeniedException(
                    "Usuario no autenticado"
            );
        }

        // Nunca confiamos en un ID enviado desde frontend.
        servicio.setId(null);

        // El dueño se obtiene desde el JWT.
        servicio.setPropietarioId(usuarioId);

        return servicioRepository.save(servicio);
    }

    // =========================
    // UPDATE
    // =========================

    public Servicio actualizar(
            String id,
            Servicio datos,
            String usuarioId,
            boolean esAdmin
    ) {

        Servicio existente =
                obtenerPorId(id);

        verificarPermiso(
                existente,
                usuarioId,
                esAdmin
        );

        validar(datos);

        // NO modificamos propietarioId.
        // El propietario original se conserva.

        existente.setPerfilId(
                datos.getPerfilId()
        );

        existente.setNombre(
                datos.getNombre()
        );

        existente.setCategoria(
                datos.getCategoria()
        );

        existente.setDescripcion(
                datos.getDescripcion()
        );

        existente.setPrecio(
                datos.getPrecio()
        );

        existente.setUbicacion(
                datos.getUbicacion()
        );

        existente.setImagen(
                datos.getImagen()
        );

        return servicioRepository.save(
                existente
        );
    }

    // =========================
    // DELETE
    // =========================

    public void eliminar(
            String id,
            String usuarioId,
            boolean esAdmin
    ) {

        Servicio servicio =
                obtenerPorId(id);

        verificarPermiso(
                servicio,
                usuarioId,
                esAdmin
        );

        servicioRepository.delete(servicio);
    }

    // =========================
    // SEGURIDAD
    // =========================

    private void verificarPermiso(
            Servicio servicio,
            String usuarioId,
            boolean esAdmin
    ) {

        // ADMIN puede administrar cualquier servicio.
        if (esAdmin) {
            return;
        }

        if (
            usuarioId == null ||
            usuarioId.isBlank() ||
            servicio.getPropietarioId() == null ||
            !servicio.getPropietarioId()
                    .equals(usuarioId)
        ) {

            throw new AccessDeniedException(
                    "No tienes permiso para modificar este servicio"
            );
        }
    }

    // =========================
    // VALIDACIONES
    // =========================

    private void validar(
            Servicio servicio
    ) {

        if (servicio == null) {

            throw new IllegalArgumentException(
                    "El servicio es obligatorio"
            );
        }

        if (
            servicio.getNombre() == null ||
            servicio.getNombre().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio"
            );
        }

        if (
            servicio.getPrecio() == null ||
            servicio.getPrecio() <= 0
        ) {

            throw new IllegalArgumentException(
                    "El precio debe ser mayor a 0"
            );
        }

        if (
            servicio.getPerfilId() == null ||
            servicio.getPerfilId().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El perfilId es obligatorio"
            );
        }
    }
}