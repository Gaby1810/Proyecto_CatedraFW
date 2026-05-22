package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "EMPLEADO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_empleado;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellido;

    @Column(nullable = false, unique = true, length = 25)
    private String identificacion;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(name = "salario_base_vigente", nullable = false)
    private Double salarioBaseVigente;
}