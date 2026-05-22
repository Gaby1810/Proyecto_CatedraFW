package sv.edu.udb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sv.edu.udb.dto.request.EmpleadoRequest;
import sv.edu.udb.dto.response.EmpleadoResponse;
import sv.edu.udb.entity.Empleado;
import sv.edu.udb.entity.SalarioBase;
import sv.edu.udb.exception.BusinessRuleException;
import sv.edu.udb.exception.ConflictException;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.repository.SalarioBaseRepository;
import sv.edu.udb.service.IEmpleadoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmpleadoServiceImpl implements IEmpleadoService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private SalarioBaseRepository salarioBaseRepository;

    @Override
    public EmpleadoResponse guardarEmpleado(EmpleadoRequest request) {
        String identificacion = request.getIdentificacion().trim();
        if (empleadoRepository.existsByIdentificacion(identificacion)) {
            throw new ConflictException("Ya existe un empleado registrado con la identificación: " + identificacion);
        }

        Empleado empleado = new Empleado();
        mapToEntity(empleado, request);
        Empleado guardado = empleadoRepository.save(empleado);
        registrarHistorialSalarial(guardado);
        return mapToResponse(guardado);
    }

    @Override
    public List<EmpleadoResponse> listarTodos() {
        return empleadoRepository.findAll(Sort.by("apellido").ascending().and(Sort.by("nombre").ascending())).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmpleadoResponse obtenerPorId(Long id) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));
        return mapToResponse(empleado);
    }

    @Override
    public EmpleadoResponse actualizarEmpleado(Long id, EmpleadoRequest request) {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + id));

        String nuevaIdentificacion = request.getIdentificacion().trim();
        if (!nuevaIdentificacion.equalsIgnoreCase(empleado.getIdentificacion())
                && empleadoRepository.existsByIdentificacion(nuevaIdentificacion)) {
            throw new ConflictException("Ya existe otro empleado con la identificación: " + nuevaIdentificacion);
        }

        Double salarioAnterior = empleado.getSalarioBaseVigente();
        mapToEntity(empleado, request);
        Empleado actualizado = empleadoRepository.save(empleado);

        if (!Objects.equals(salarioAnterior, actualizado.getSalarioBaseVigente())) {
            registrarHistorialSalarial(actualizado);
        }

        return mapToResponse(actualizado);
    }

    @Override
    public void eliminarEmpleado(Long id) {
        if (!empleadoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se encontró el empleado con ID: " + id);
        }

        try {
            empleadoRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessRuleException("No se puede eliminar el empleado porque tiene planillas, usuarios o historial asociado");
        }
    }

    private void registrarHistorialSalarial(Empleado empleado) {
        SalarioBase salarioBase = new SalarioBase();
        salarioBase.setEmpleado(empleado);
        salarioBase.setSalario(empleado.getSalarioBaseVigente());
        salarioBase.setFechaVigencia(LocalDate.now());
        salarioBaseRepository.save(salarioBase);
    }

    private void mapToEntity(Empleado empleado, EmpleadoRequest request) {
        empleado.setNombre(normalizarTexto(request.getNombre()));
        empleado.setApellido(normalizarTexto(request.getApellido()));
        empleado.setIdentificacion(request.getIdentificacion().trim());
        empleado.setDireccion(request.getDireccion() == null ? null : request.getDireccion().trim());
        empleado.setTipo(request.getTipo().trim().toUpperCase(Locale.ROOT));
        empleado.setSalarioBaseVigente(request.getSalarioBaseVigente());
    }

    private EmpleadoResponse mapToResponse(Empleado empleado) {
        EmpleadoResponse response = new EmpleadoResponse();
        response.setId_empleado(empleado.getId_empleado());
        response.setNombre(empleado.getNombre());
        response.setApellido(empleado.getApellido());
        response.setIdentificacion(empleado.getIdentificacion());
        response.setDireccion(empleado.getDireccion());
        response.setTipo(empleado.getTipo());
        response.setSalarioBaseVigente(empleado.getSalarioBaseVigente());
        response.setNombreCompleto(empleado.getNombre() + " " + empleado.getApellido());
        return response;
    }

    private String normalizarTexto(String valor) {
        String limpio = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (limpio.isBlank()) {
            return limpio;
        }

        String[] partes = limpio.split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String parte : partes) {
            if (!resultado.isEmpty()) {
                resultado.append(' ');
            }
            resultado.append(Character.toUpperCase(parte.charAt(0)))
                    .append(parte.substring(1));
        }
        return resultado.toString();
    }
}
