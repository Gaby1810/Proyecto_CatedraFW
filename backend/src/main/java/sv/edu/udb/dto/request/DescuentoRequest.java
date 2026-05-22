package sv.edu.udb.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DescuentoRequest {

    @NotBlank(message = "El tipo de descuento es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String tipo;

    @NotNull(message = "El porcentaje es obligatorio")
    @PositiveOrZero(message = "El porcentaje no puede ser negativo")
    @DecimalMax(value = "100.0", message = "El porcentaje no puede superar 100")
    private Double porcentaje;

    @NotNull(message = "La fecha de vigencia es obligatoria")
    private LocalDate vigencia;
}
