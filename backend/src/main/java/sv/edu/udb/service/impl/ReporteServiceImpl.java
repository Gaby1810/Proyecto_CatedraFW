package sv.edu.udb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.response.DashboardResponse;
import sv.edu.udb.dto.response.DescuentoAplicadoResponse;
import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.dto.response.ReportePlanillaResponse;
import sv.edu.udb.dto.response.ResumenPeriodoDTO;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.entity.Planilla;
import sv.edu.udb.entity.PlanillaDescuento;
import sv.edu.udb.exception.ResourceNotFoundException;
import sv.edu.udb.repository.PlanillaDescuentoRepository;
import sv.edu.udb.repository.PlanillaRepository;
import sv.edu.udb.service.CurrentUserService;
import sv.edu.udb.service.IReporteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements IReporteService {

    @Autowired
    private PlanillaRepository planillaRepository;

    @Autowired
    private PlanillaDescuentoRepository planillaDescuentoRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public PlanillaResponse obtenerBoleta(Long idPlanilla) {
        Planilla planilla = planillaRepository.findById(idPlanilla)
                .orElseThrow(() -> new ResourceNotFoundException("Planilla no encontrada con ID: " + idPlanilla));
        currentUserService.validatePlanillaAccess(planilla);
        return mapToPlanillaResponse(planilla);
    }

    @Override
    public List<PlanillaResponse> obtenerBoletasPorEmpleado(Long idEmpleado) {
        currentUserService.validateEmployeeAccess(idEmpleado);
        return planillaRepository.buscarPorIdEmpleado(idEmpleado).stream()
                .map(this::mapToPlanillaResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlanillaResponse> obtenerMisBoletas() {
        Long empleadoId = currentUserService.getCurrentUser().getEmpleadoId();
        if (empleadoId == null) {
            throw new ResourceNotFoundException("Tu usuario no está vinculado a ningún empleado");
        }
        return obtenerBoletasPorEmpleado(empleadoId);
    }

    @Override
    public ReportePlanillaResponse generarReportePorPeriodo(String periodo) {
        List<PlanillaResponse> planillas = planillaRepository.findByPeriodo(periodo.trim().toUpperCase()).stream()
                .map(this::mapToPlanillaResponse)
                .collect(Collectors.toList());

        ReportePlanillaResponse reporte = new ReportePlanillaResponse();
        reporte.setPeriodo(periodo.trim().toUpperCase());
        reporte.setCantidadPlanillas(planillas.size());
        reporte.setTotalSalarioBruto(planillas.stream().mapToDouble(PlanillaResponse::getSalarioBruto).sum());
        reporte.setTotalDescuentos(planillas.stream().mapToDouble(PlanillaResponse::getTotalDescuentos).sum());
        reporte.setTotalSalarioNeto(planillas.stream().mapToDouble(PlanillaResponse::getSalarioNeto).sum());
        reporte.setPlanillas(planillas);
        return reporte;
    }

    @Override
    public DashboardResponse getDashboard() {
        DashboardResponse dash = new DashboardResponse();
        dash.setTotalEmpleados(empleadoRepository.count());
        dash.setTotalPlanillas(planillaRepository.count());
        dash.setTotalSalarioBruto(nvl(planillaRepository.sumSalarioBruto()));
        dash.setTotalDescuentos(nvl(planillaRepository.sumTotalDescuentos()));
        dash.setTotalSalarioNeto(nvl(planillaRepository.sumSalarioNeto()));

        List<ResumenPeriodoDTO> resumen = planillaRepository.findResumenPorPeriodo().stream()
                .map(row -> new ResumenPeriodoDTO(
                        (String)  row[0],
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue()))
                .collect(Collectors.toList());
        dash.setResumenPorPeriodo(resumen);
        dash.setPeriodosActivos(resumen.size());

        List<PlanillaResponse> ultimas = planillaRepository.findUltimas6().stream()
                .map(this::mapToPlanillaResponse)
                .collect(Collectors.toList());
        dash.setUltimasPlanillas(ultimas);

        return dash;
    }

    private double nvl(Double v) { return v == null ? 0.0 : v; }

    private PlanillaResponse mapToPlanillaResponse(Planilla planilla) {
        List<PlanillaDescuento> detalles = planillaDescuentoRepository.buscarPorIdPlanilla(planilla.getId_planilla());

        PlanillaResponse response = new PlanillaResponse();
        response.setId_planilla(planilla.getId_planilla());
        response.setPeriodo(planilla.getPeriodo());
        response.setFechaGeneracion(planilla.getFechaGeneracion());
        response.setHorasTrabajadas(planilla.getHorasTrabajadas());
        response.setBonificacion(planilla.getBonificacion());
        response.setSalarioBruto(planilla.getSalarioBruto());
        response.setTotalDescuentos(planilla.getTotalDescuentos());
        response.setSalarioNeto(planilla.getSalarioNeto());
        response.setIdEmpleado(planilla.getEmpleado().getId_empleado());
        response.setNombreEmpleado(planilla.getEmpleado().getNombre() + " " + planilla.getEmpleado().getApellido());
        response.setDescuentosAplicados(detalles.stream().map(this::mapDetalleDescuento).collect(Collectors.toList()));
        response.setDetallesDescuentos(response.getDescuentosAplicados().stream()
                .map(descuento -> descuento.getTipo() + ": $" + descuento.getMontoDescuento())
                .collect(Collectors.joining(", ")));
        return response;
    }

    private DescuentoAplicadoResponse mapDetalleDescuento(PlanillaDescuento detalle) {
        DescuentoAplicadoResponse response = new DescuentoAplicadoResponse();
        response.setId_descuento(detalle.getDescuento().getId_descuento());
        response.setTipo(detalle.getDescuento().getTipo());
        response.setPorcentaje(detalle.getDescuento().getPorcentaje());
        response.setMontoDescuento(detalle.getMontoDescuento());
        return response;
    }
}
