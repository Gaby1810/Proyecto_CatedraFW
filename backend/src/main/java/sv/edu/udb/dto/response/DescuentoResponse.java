package sv.edu.udb.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DescuentoResponse {
    private Long id_descuento;
    private String tipo;
    private Double porcentaje;
    private LocalDate vigencia;
}
