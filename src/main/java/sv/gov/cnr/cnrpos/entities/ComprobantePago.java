package sv.gov.cnr.cnrpos.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "CNRPOS_COMPROBANTE_PAGO_TRANSACCION")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobantePago {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SQ_COMPROBANTE_PAGOS")
    @SequenceGenerator(name = "SQ_COMPROBANTE_PAGOS", sequenceName = "SQ_COMPROBANTE_PAGOS", allocationSize = 1)
    @Column(name = "ID_COMPROBANTE")
    private Long idComprobante;

    @ManyToOne
    @JoinColumn(name = "ID_TRANSACCION")
    @JsonBackReference
    private Transaccion transaccion;

    @Column(name = "NUMERO_COMPROBANTE")
    private String numeroComprobante;
}
