package privelink.backend.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import privelink.backend.model.Servicio;

@Repository
public interface ServicioRepository extends MongoRepository<Servicio, String> {

    List<Servicio> findByPerfilId(String perfilId);
}