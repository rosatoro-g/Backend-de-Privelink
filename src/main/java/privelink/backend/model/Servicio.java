package privelink.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "servicios")
public class Servicio {

    @Id
    private String id;

    // Usuario real dueño del servicio.
    // Se obtiene automáticamente desde el JWT.
    private String propietarioId;

    // ID del perfil visual de PrivéLink.
    private String perfilId;

    private String nombre;
    private String categoria;
    private String descripcion;
    private Integer precio;
    private String ubicacion;
    private String imagen;
}