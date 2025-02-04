package sv.gov.cnr.cnrpos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.gov.cnr.cnrpos.entities.RCatalogos;

import java.util.List;

@Repository
public interface RcatalogoRepository extends JpaRepository<RCatalogos, Long> {

    List<RCatalogos> findByGrupo(String grupo);

    List<RCatalogos> findByIdCatalogo(Long id);
}
