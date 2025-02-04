package sv.gov.cnr.cnrpos.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductoResponse {

    private int cod_id;
    private String clasificacion;
    private String codigo_producto;
    private String nombre;
    private String descripcion;
    private String codigo_ingreso;
    private double precio;
    private double iva;
    private String tipo;
    private double total;
    private String estado;
    private String editable;
}
