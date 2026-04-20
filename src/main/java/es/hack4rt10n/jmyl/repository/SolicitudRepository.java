package es.hack4rt10n.jmyl.repository;

import es.hack4rt10n.jmyl.model.Solicitud;
import es.hack4rt10n.jmyl.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByUsuario(Usuario usuario);

    List<Solicitud> findByEstado(String estado);

    List<Solicitud> findByCategoria(String categoria);

    List<Solicitud> findByEstadoAndCategoria(String estado, String categoria);

    @Query("SELECT s FROM Solicitud s WHERE YEAR(s.fechaCreacion) = :anio")
    List<Solicitud> findByAnio(@Param("anio") int anio);

    @Query("SELECT s FROM Solicitud s WHERE YEAR(s.fechaCreacion) = :anio AND s.estado = :estado")
    List<Solicitud> findByAnioAndEstado(@Param("anio") int anio, @Param("estado") String estado);

    @Query("SELECT s FROM Solicitud s WHERE YEAR(s.fechaCreacion) = :anio AND s.categoria = :categoria")
    List<Solicitud> findByAnioAndCategoria(@Param("anio") int anio, @Param("categoria") String categoria);

    @Query("SELECT s FROM Solicitud s WHERE YEAR(s.fechaCreacion) = :anio AND s.estado = :estado AND s.categoria = :categoria")
    List<Solicitud> findByAnioAndEstadoAndCategoria(@Param("anio") int anio, @Param("estado") String estado, @Param("categoria") String categoria);

    @Query("SELECT DISTINCT YEAR(s.fechaCreacion) FROM Solicitud s ORDER BY YEAR(s.fechaCreacion) DESC")
    List<Integer> findAniosDisponibles();
}


