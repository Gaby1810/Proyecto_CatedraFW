package sv.edu.udb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.request.PlanillaRequest;
import sv.edu.udb.dto.response.DescuentoAplicadoResponse;
import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.entity.Descuento;
import sv.edu.udb.entity.Empleado;
import sv.edu.udb.entity.HorasClase;
import sv.edu.udb.entity.Planilla;
import sv.edu.udb.entity.PlanillaDescuento;
import sv.edu.udb.exception.ConflictException;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.repository.DescuentoRepository;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.repository.HorasClaseRepository;
import sv.edu.udb.repository.PlanillaDescuentoRepository;
import sv.edu.udb.repository.PlanillaRepository;
import sv.edu.udb.service.IPlanillaService;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanillaServiceImpl implements IPlanillaService {

    @Autowired
    private PlanillaRepository planillaRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private PlanillaDescuentoRepository planillaDescuentoRepository;

    @Autowired
    private HorasClaseRepository horasClaseRepository;

    @Autowired
    private CalculadoraPlanilla calculadoraPlanilla;

    @Override
    public PlanillaResponse procesarPlanillaEmpleado(PlanillaRequest request) {
        Empleado empleado = empleadoRepository.findById(request.getIdEmpleado())
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado con ID: " + request.getIdEmpleado()));

        String periodo = normalizarPeriodo(request.getPeriodo());
        if (planillaRepository.existsForEmployeeAndPeriod(empleado.getId_empleado(), periodo)) {
            throw new ConflictException("Ya existe una planilla para este empleado en el período " + periodo);
        }

        Double salarioBruto = calculadoraPlanilla.calcularSalarioBruto(
                empleado.getSalarioBaseVigente(),
                request.getHorasTrabajadas(),
                request.getBonificacion()
        );
        Double isss = calculadoraPlanilla.calcularISSS(salarioBruto);
        Double afp = calculadoraPlanilla.calcularAFP(salarioBruto);
        Double renta = calculadoraPlanilla.calcularRenta(salarioBruto - isss - afp);
        Double totalDescuentos = calculadoraPlanilla.calcularTotalDescuentos(isss, afp, renta);
        Double salarioNeto = calculadoraPlanilla.calcularSalarioNeto(salarioBruto, totalDescuentos);

        Planilla planilla = new Planilla();
        planilla.setEmpleado(empleado);
        planilla.setPeriodo(periodo);
        planilla.setFechaGeneracion(LocalDate.now());
        planilla.setHorasTrabajadas(request.getHorasTrabajadas());
        planilla.setBonificacion(request.getBonificacion());
        planilla.setSalarioBruto(salarioBruto);
        planilla.setTotalDescuentos(totalDescuentos);
        planilla.setSalarioNeto(salarioNeto);
        planilla = planillaRepository.save(planilla);

        registrarHorasClase(empleado, periodo, request.getHorasTrabajadas());
        guardarDetalleDescuento(planilla, "ISSS", isss);
        guardarDetalleDescuento(planilla, "AFP", afp);
        guardarDetalleDescuento(planilla, "RENTA", renta);

        return mapToResponse(planilla);
    }

    @Override
    public List<PlanillaResponse> listarHistorialPorEmpleado(Long idEmpleado) {
        if (!empleadoRepository.existsById(idEmpleado)) {
            throw new ResourceNotFoundException("Empleado no encontrado con ID: " + idEmpleado);
        }

        return planillaRepository.buscarPorIdEmpleado(idEmpleado).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void registrarHorasClase(Empleado empleado, String periodo, Double horasTrabajadas) {
        HorasClase horasClase = horasClaseRepository.findByEmpleadoAndPeriodo(empleado.getId_empleado(), periodo)
                .orElseGet(HorasClase::new);
        horasClase.setEmpleado(empleado);
        horasClase.setPeriodo(periodo);
        horasClase.setHorasTrabajadas(horasTrabajadas);
        horasClaseRepository.save(horasClase);
    }

    private void guardarDetalleDescuento(Planilla planilla, String tipo, Double monto) {
        Descuento descuento = descuentoRepository.findTopByTipoIgnoreCaseOrderByVigenciaDesc(tipo)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró un descuento configurado para " + tipo));

        PlanillaDescuento detalle = new PlanillaDescuento();
        detalle.setPlanilla(planilla);
        detalle.setDescuento(descuento);
        detalle.setMontoDescuento(monto);
        planillaDescuentoRepository.save(detalle);
    }

    private PlanillaResponse mapToResponse(Planilla planilla) {
        List<PlanillaDescuento> detalles = planillaDescuentoRepository.buscarPorIdPlanilla(planilla.getId_planilla());

        PlanillaResponse response = new PlanillaResponse();
        response.setId_planilla(planilla.getId_planilla());
        response.setIdEmpleado(planilla.getEmpleado().getId_empleado());
        response.setNombreEmpleado(planilla.getEmpleado().getNombre() + " " + planilla.getEmpleado().getApellido());
        response.setPeriodo(planilla.getPeriodo());
        response.setFechaGeneracion(planilla.getFechaGeneracion());
        response.setHorasTrabajadas(planilla.getHorasTrabajadas());
        response.setBonificacion(planilla.getBonificacion());
        response.setSalarioBruto(planilla.getSalarioBruto());
        response.setTotalDescuentos(planilla.getTotalDescuentos());
        response.setSalarioNeto(planilla.getSalarioNeto());
        response.setDescuentosAplicados(detalles.stream().map(this::mapDetalle).collect(Collectors.toList()));
        response.setDetallesDescuentos(response.getDescuentosAplicados().stream()
                .map(item -> item.getTipo() + ": $" + item.getMontoDescuento())
                .collect(Collectors.joining(", ")));
        return response;
    }

    private DescuentoAplicadoResponse mapDetalle(PlanillaDescuento detalle) {
        DescuentoAplicadoResponse response = new DescuentoAplicadoResponse();
        response.setId_descuento(detalle.getDescuento().getId_descuento());
        response.setTipo(detalle.getDescuento().getTipo());
        response.setPorcentaje(detalle.getDescuento().getPorcentaje());
        response.setMontoDescuento(detalle.getMontoDescuento());
        return response;
    }

    private String normalizarPeriodo(String periodo) {
        return periodo.trim().toUpperCase(Locale.ROOT);
    }
}
