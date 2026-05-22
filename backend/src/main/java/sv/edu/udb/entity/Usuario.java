package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuario") // En minúsculas para mantener consistencia con tus logs de Hibernate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"rol", "empleado"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id_usuario;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    @Column(nullable = false, length = 255)
    private String contrasena; 

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_empleado", nullable = true)
    private Empleado empleado;
}
