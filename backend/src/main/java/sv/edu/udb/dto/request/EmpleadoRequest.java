package sv.edu.udb.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sv.edu.udb.validation.ValidDui;

@Data
public class EmpleadoRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(max = 80, message = "El apellido no puede exceder 80 caracteres")
    private String apellido;

    @NotBlank(message = "La identificación (DUI) es obligatoria")
    @ValidDui
    private String identificacion;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @NotBlank(message = "El tipo (DOCENTE/ADMINISTRATIVO) es obligatorio")
    @Pattern(
            regexp = "^(DOCENTE|ADMINISTRATIVO)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El tipo debe ser DOCENTE o ADMINISTRATIVO"
    )
    private String tipo;

    @NotNull(message = "El salario base es obligatorio")
    @DecimalMin(value = "0.01", message = "El salario debe ser mayor a cero")
    private Double salarioBaseVigente;
}
