package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PLANILLA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_planilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_empleado",
            nullable = false
    )
    private Empleado empleado;

    @Column(nullable = false,length = 20)
    private String periodo;

    @Column(
            name="fecha_generacion",
            nullable=false
    )
    private LocalDate fechaGeneracion;

    @Column(name = "horas_trabajadas", nullable = false)
    private Double horasTrabajadas;

    @Column(nullable = false)
    private Double bonificacion;

    @Column(
            name="salario_bruto",
            nullable=false
    )
    private Double salarioBruto;

    @Column(
            name="total_descuentos",
            nullable=false
    )
    private Double totalDescuentos;

    @Column(
            name="salario_neto",
            nullable=false
    )
    private Double salarioNeto;

}
