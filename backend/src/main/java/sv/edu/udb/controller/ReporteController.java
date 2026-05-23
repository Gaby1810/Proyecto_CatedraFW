package sv.edu.udb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.dto.response.DashboardResponse;
import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.dto.response.ReportePlanillaResponse;
import sv.edu.udb.service.IReporteService;

import java.util.List;

@RestController
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private IReporteService reporteService;

    @GetMapping("/boletas/{idPlanilla}")
    public ResponseEntity<PlanillaResponse> obtenerBoleta(@PathVariable Long idPlanilla) {
        return ResponseEntity.ok(reporteService.obtenerBoleta(idPlanilla));
    }

    @GetMapping("/empleado/{idEmpleado}/boletas")
    public ResponseEntity<List<PlanillaResponse>> obtenerBoletasPorEmpleado(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(reporteService.obtenerBoletasPorEmpleado(idEmpleado));
    }

    @GetMapping("/mis-boletas")
    public ResponseEntity<List<PlanillaResponse>> obtenerMisBoletas() {
        return ResponseEntity.ok(reporteService.obtenerMisBoletas());
    }

    @GetMapping("/periodo/{periodo}")
    public ResponseEntity<ReportePlanillaResponse> generarReportePorPeriodo(@PathVariable String periodo) {
        return ResponseEntity.ok(reporteService.generarReportePorPeriodo(periodo));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(reporteService.getDashboard());
    }
}
