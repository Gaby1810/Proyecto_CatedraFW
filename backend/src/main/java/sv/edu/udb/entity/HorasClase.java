package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HORAS_CLASE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorasClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_horas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado", nullable = false)
    private Empleado empleado;

    @Column(nullable = false, length = 20)
    private String periodo; 

    @Column(name = "horas_trabajadas", nullable = false)
    private Double horasTrabajadas;
}