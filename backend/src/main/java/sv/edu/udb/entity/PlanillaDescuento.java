package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PLANILLA_DESCUENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanillaDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_planilla_descuento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_planilla", nullable = false)
    private Planilla planilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_descuento", nullable = false)
    private Descuento descuento;

    @Column(name = "monto_descuento", nullable = false)
    private Double montoDescuento;
}