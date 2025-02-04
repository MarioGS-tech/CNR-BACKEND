package sv.gov.cnr.cnrpos.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.gov.cnr.cnrpos.entities.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item,Long> {

    @Query(value = "SELECT it.*" +
            "FROM CNRPOS_TRANSACCION tr " +
            "INNER JOIN CNRPOS_ITEM it ON it.ID_TRANSACCION = tr.ID_TRANSACCION " +
            "WHERE tr.CODIGO_GENERACION= :codigoGeneracion", nativeQuery = true)
    List<Item> itemsByDte(@Param("codigoGeneracion") String codigoGeneracion);

    @Query(value = "SELECT it.*" +
            "FROM CNRPOS_TRANSACCION tr " +
            "INNER JOIN CNRPOS_ITEM it ON it.ID_TRANSACCION = tr.ID_TRANSACCION " +
            "WHERE it.NRO_DOCUMENTO = :codigoGeneracion AND tr.status = 2 " +
            "AND tr.TIPO_DTE = '5' ", nativeQuery = true)
    List<Item> itemsUsadosNotas(@Param("codigoGeneracion") String codigoGeneracion);



    @Query(value = "SELECT it.*" +
            "FROM CNRPOS_TRANSACCION tr " +
            "INNER JOIN CNRPOS_ITEM it ON it.ID_TRANSACCION = tr.ID_TRANSACCION " +
            "WHERE tr.CODIGO_GENERACION IN :codigosGeneracion", nativeQuery = true)
    List<Item> itemsByDtes(@Param("codigosGeneracion") List<String> codigosGeneracion);


}
