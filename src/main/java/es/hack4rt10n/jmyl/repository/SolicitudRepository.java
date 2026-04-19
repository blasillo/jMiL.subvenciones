package es.hack4rt10n.jmyl.repository;

import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    List<Solicitud> findByUsuario(Usuario usuario);
    List<Solicitud> findByEstado(String estado);
}


