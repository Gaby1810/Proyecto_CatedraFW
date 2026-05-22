package sv.edu.udb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sv.edu.udb.dto.request.PlanillaRequest;
import sv.edu.udb.dto.response.PlanillaResponse;
import sv.edu.udb.entity.Descuento;
import sv.edu.udb.entity.Empleado;
import sv.edu.udb.exception.ConflictException;
import sv.edu.udb.repository.DescuentoRepository;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.repository.HorasClaseRepository;
import sv.edu.udb.repository.PlanillaDescuentoRepository;
import sv.edu.udb.repository.PlanillaRepository;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.SalarioBaseRepository;
import sv.edu.udb.repository.UsuarioRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PlanillaServiceIntegrationTests {

    @Autowired
    private IPlanillaService planillaService;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private PlanillaRepository planillaRepository;

    @Autowired
    private PlanillaDescuentoRepository planillaDescuentoRepository;

    @Autowired
    private HorasClaseRepository horasClaseRepository;

    @Autowired
    private SalarioBaseRepository salarioBaseRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    private Long empleadoId;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        planillaDescuentoRepository.deleteAll();
        planillaRepository.deleteAll();
        horasClaseRepository.deleteAll();
        salarioBaseRepository.deleteAll();
        descuentoRepository.deleteAll();
        empleadoRepository.deleteAll();
        rolRepository.deleteAll();

        Empleado empleado = new Empleado();
        empleado.setNombre("Paula");
        empleado.setApellido("Santos");
        empleado.setIdentificacion("34567890-1");
        empleado.setDireccion("San Salvador");
        empleado.setTipo("DOCENTE");
        empleado.setSalarioBaseVigente(800.00);
        empleadoId = empleadoRepository.save(empleado).getId_empleado();

        descuentoRepository.save(buildDiscount("ISSS", 3.00));
        descuentoRepository.save(buildDiscount("AFP", 7.25));
        descuentoRepository.save(buildDiscount("RENTA", 0.00));
    }

    @Test
    void shouldProcessPayrollAndGenerateThreeDiscounts() {
        PlanillaRequest request = new PlanillaRequest();
        request.setIdEmpleado(empleadoId);
        request.setPeriodo("05-2026");
        request.setHorasTrabajadas(160.0);
        request.setBonificacion(25.0);

        PlanillaResponse response = planillaService.procesarPlanillaEmpleado(request);

        assertEquals("05-2026", response.getPeriodo());
        assertEquals(825.00, response.getSalarioBruto());
        assertEquals(3, response.getDescuentosAplicados().size());
    }

    @Test
    void shouldRejectDuplicatePayrollForSameEmployeeAndPeriod() {
        PlanillaRequest request = new PlanillaRequest();
        request.setIdEmpleado(empleadoId);
        request.setPeriodo("05-2026");
        request.setHorasTrabajadas(160.0);
        request.setBonificacion(0.0);

        planillaService.procesarPlanillaEmpleado(request);

        assertThrows(ConflictException.class, () -> planillaService.procesarPlanillaEmpleado(request));
    }

    private Descuento buildDiscount(String tipo, Double porcentaje) {
        Descuento descuento = new Descuento();
        descuento.setTipo(tipo);
        descuento.setPorcentaje(porcentaje);
        descuento.setVigencia(LocalDate.now());
        return descuento;
    }
}
