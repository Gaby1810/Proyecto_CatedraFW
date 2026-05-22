package sv.edu.udb.service;

import sv.edu.udb.dto.request.PlanillaRequest;
import sv.edu.udb.dto.response.PlanillaResponse;
import java.util.List;

public interface IPlanillaService {
    
    PlanillaResponse procesarPlanillaEmpleado(PlanillaRequest request);
    
    List<PlanillaResponse> listarHistorialPorEmpleado(Long idEmpleado);
}