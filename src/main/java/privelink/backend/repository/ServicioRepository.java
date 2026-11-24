package privelink.backend.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import privelink.backend.model.Servicio;

public interface ServicioRepository extends MongoRepository<Servicio, String> {

    List<Servicio> findByPerfilId(String perfilId);
}