package privelink.backend.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ControladorPruebasSeguridad {

    @GetMapping("/api/admin/prueba-acceso")
    public String admin() {
        return "Acceso ADMIN permitido";
    }

    @GetMapping("/api/colaborador/prueba-acceso")
    public String colaborador() {
        return "Acceso COLABORADOR permitido";
    }

    @GetMapping("/api/cliente/prueba-acceso")
    public String cliente() {
        return "Acceso CLIENTE permitido";
    }
}