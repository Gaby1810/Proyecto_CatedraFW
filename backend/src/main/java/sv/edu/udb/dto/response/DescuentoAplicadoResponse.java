package sv.edu.udb.dto.response;

import lombok.Data;

@Data
public class DescuentoAplicadoResponse {
    private Long id_descuento;
    private String tipo;
    private Double porcentaje;
    private Double montoDescuento;
}
