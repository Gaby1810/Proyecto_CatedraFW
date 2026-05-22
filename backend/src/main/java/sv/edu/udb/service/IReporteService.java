package sv.edu.udb.service;

import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.dto.response.ReportePlanillaResponse;

import java.util.List;

public interface IReporteService {
    PlanillaResponse obtenerBoleta(Long idPlanilla);

    List<PlanillaResponse> obtenerBoletasPorEmpleado(Long idEmpleado);

    List<PlanillaResponse> obtenerMisBoletas();

    ReportePlanillaResponse generarReportePorPeriodo(String periodo);
}
