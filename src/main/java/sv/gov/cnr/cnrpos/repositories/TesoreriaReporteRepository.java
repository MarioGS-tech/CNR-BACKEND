package sv.gov.cnr.cnrpos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.gov.cnr.cnrpos.entities.TesoreriaReporte;

import java.util.List;

@Repository
public interface TesoreriaReporteRepository extends JpaRepository<TesoreriaReporte, String> {

    @Query("SELECT R FROM TesoreriaReporte R WHERE R.deptoReporte = :deptoReporte")
    public List<TesoreriaReporte> findAllTesoreria(@Param("deptoReporte") String deptoReporte);

    @Query("SELECT R FROM TesoreriaReporte R WHERE R.deptoReporte = :deptoReporte")
    public List<TesoreriaReporte> findAllConta(@Param("deptoReporte") String deptoReporte);

    @Query("SELECT r FROM TesoreriaReporte r WHERE r.deptoReporte = :deptoReporte ORDER BY r.clasificacion")
    public List<TesoreriaReporte> findAllOrderByClasificacion(@Param("deptoReporte") String deptoReporte);
}
