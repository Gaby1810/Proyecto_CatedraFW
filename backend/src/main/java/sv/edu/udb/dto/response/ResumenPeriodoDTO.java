package sv.edu.udb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResumenPeriodoDTO {
    private String periodo;
    private long cantidadPlanillas;
    private double totalSalarioNeto;
}
