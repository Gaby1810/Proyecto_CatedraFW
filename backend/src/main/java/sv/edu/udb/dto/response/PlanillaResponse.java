package sv.edu.udb.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class PlanillaResponse {
    private Long id_planilla;
    private Long idEmpleado;
    private String nombreEmpleado;
    private String periodo;
    private LocalDate fechaGeneracion;
    private Double horasTrabajadas;
    private Double bonificacion;
    private Double salarioBruto;
    private Double totalDescuentos;
    private Double salarioNeto;
    private String detallesDescuentos;
    private List<DescuentoAplicadoResponse> descuentosAplicados;
}
