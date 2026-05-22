package sv.edu.udb.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "DESCUENTO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_descuento;

    @Column(nullable = false, unique = true, length = 50)
    private String tipo; // Ejemplo: "ISSS", "AFP", "RENTA"

    @Column(nullable = false)
    private Double porcentaje;

    @Column(nullable = false)
    private LocalDate vigencia;
}