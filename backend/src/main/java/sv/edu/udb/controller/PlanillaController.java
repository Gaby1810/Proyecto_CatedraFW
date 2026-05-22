package sv.edu.udb.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.request.PlanillaRequest;
import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.service.IPlanillaService;

import java.util.List;

@RestController
@RequestMapping("/planillas")
public class PlanillaController {

    @Autowired
    private IPlanillaService planillaService;

    @PostMapping("/procesar")
    public ResponseEntity<PlanillaResponse> procesar(@Valid @RequestBody PlanillaRequest request) {
        return ResponseEntity.ok(planillaService.procesarPlanillaEmpleado(request));
    }

    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<List<PlanillaResponse>> getHistorial(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(planillaService.listarHistorialPorEmpleado(idEmpleado));
    }
}
