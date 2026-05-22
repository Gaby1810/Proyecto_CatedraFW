package sv.edu.udb.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.dto.request.DescuentoRequest;
import sv.edu.udb.dto.response.DescuentoResponse;
import sv.edu.udb.service.IDescuentoService;

import java.util.List;

@RestController
@RequestMapping("/descuentos")
public class DescuentoController {

    @Autowired
    private IDescuentoService descuentoService;

    @PostMapping
    public ResponseEntity<DescuentoResponse> guardar(@Valid @RequestBody DescuentoRequest request) {
        return ResponseEntity.ok(descuentoService.guardarDescuento(request));
    }

    @GetMapping
    public ResponseEntity<List<DescuentoResponse>> listar() {
        return ResponseEntity.ok(descuentoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DescuentoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(descuentoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DescuentoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody DescuentoRequest request) {
        return ResponseEntity.ok(descuentoService.actualizarDescuento(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        descuentoService.eliminarDescuento(id);
        return ResponseEntity.noContent().build();
    }
}
