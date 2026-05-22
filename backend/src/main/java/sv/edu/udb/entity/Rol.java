package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ROL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_rol;

    @Column(name = "nombre_rol", nullable = false, unique = true, length = 50)
    private String nombreRol;

    @Column(length = 150)
    private String descripcion;
}