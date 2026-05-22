package sv.edu.udb.dto.response;

import lombok.Data;

@Data
public class EmpleadoResponse {
    private Long id_empleado;
    private String nombre;
    private String apellido;
    private String identificacion;
    private String direccion;
    private String tipo;
    private Double salarioBaseVigente;
    private String nombreCompleto;
}
