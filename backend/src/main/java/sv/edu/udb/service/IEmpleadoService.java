package sv.edu.udb.service;

import sv.edu.udb.dto.request.EmpleadoRequest;
import sv.edu.udb.dto.response.EmpleadoResponse;
import java.util.List;

public interface IEmpleadoService {
    
    EmpleadoResponse guardarEmpleado(EmpleadoRequest request);
    
    EmpleadoResponse actualizarEmpleado(Long id, EmpleadoRequest request);
    
    List<EmpleadoResponse> listarTodos();
    
    EmpleadoResponse obtenerPorId(Long id);
    
    void eliminarEmpleado(Long id);
}