package privelink.backend.dto;

import lombok.Getter;
import lombok.Setter;
import privelink.backend.model.RolUsuario;

@Getter
@Setter
public class LoginResponse {

    private String id;
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private String token;

}