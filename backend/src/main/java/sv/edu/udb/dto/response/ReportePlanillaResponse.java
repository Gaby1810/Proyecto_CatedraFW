package sv.edu.udb.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ReportePlanillaResponse {
    private String periodo;
    private Integer cantidadPlanillas;
    private Double totalSalarioBruto;
    private Double totalDescuentos;
    private Double totalSalarioNeto;
    private List<PlanillaResponse> planillas;
}
