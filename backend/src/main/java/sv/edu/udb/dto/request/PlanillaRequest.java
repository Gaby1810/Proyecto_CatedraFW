package sv.edu.udb.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PlanillaRequest {
    @NotNull(message = "El ID del empleado es necesario")
    private Long idEmpleado;

    @NotBlank(message = "El periodo es obligatorio (ej: 03-2026)")
    @Pattern(
            regexp = "^(0[1-9]|1[0-2])-[0-9]{4}$",
            message = "El período debe cumplir el formato MM-YYYY"
    )
    private String periodo;

    @NotNull(message = "Las horas trabajadas son obligatorias")
    @DecimalMin(value = "1.0", message = "Las horas trabajadas deben ser mayores a cero")
    @DecimalMax(value = "744.0", message = "Las horas trabajadas no pueden superar 744 por mes")
    private Double horasTrabajadas;

    @NotNull(message = "La bonificación es obligatoria, usa 0 si no aplica")
    @DecimalMin(value = "0.0", message = "La bonificación no puede ser negativa")
    private Double bonificacion;
}
