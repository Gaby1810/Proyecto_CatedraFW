package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "SALARIO_BASE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalarioBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_salario_base;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(nullable = false)
    private Double salario;

    @Column(name = "fecha_vigencia", nullable = false)
    private LocalDate fechaVigencia;
}