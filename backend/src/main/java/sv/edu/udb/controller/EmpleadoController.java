package sv.edu.udb.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sv.edu.udb.dto.request.EmpleadoRequest;
import sv.edu.udb.dto.response.EmpleadoResponse;
import sv.edu.udb.service.IEmpleadoService;

import java.util.List;

@RestController
@RequestMapping("/empleados")
public class EmpleadoController {

    @Autowired
    private IEmpleadoService empleadoService;

    @PostMapping
    public ResponseEntity<EmpleadoResponse> guardar(@Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.guardarEmpleado(request));
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> listar() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.actualizarEmpleado(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empleadoService.eliminarEmpleado(id);
        return ResponseEntity.noContent().build();
    }
}
