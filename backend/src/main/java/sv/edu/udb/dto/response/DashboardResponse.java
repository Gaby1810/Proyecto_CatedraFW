package sv.edu.udb.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DashboardResponse {
    private long totalEmpleados;
    private long totalPlanillas;
    private double totalSalarioBruto;
    private double totalDescuentos;
    private double totalSalarioNeto;
    private int periodosActivos;
    private List<ResumenPeriodoDTO> resumenPorPeriodo;
    private List<PlanillaResponse> ultimasPlanillas;
}
