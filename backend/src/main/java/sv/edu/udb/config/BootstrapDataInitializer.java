package sv.edu.udb.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sv.edu.udb.entity.Descuento;
import sv.edu.udb.entity.Empleado;
import sv.edu.udb.entity.Rol;
import sv.edu.udb.entity.Usuario;
import sv.edu.udb.repository.DescuentoRepository;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

import java.time.LocalDate;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {

    @Value("${app.bootstrap-demo-data:true}")
    private boolean bootstrapDemoData;

    @Value("${app.demo.admin.username:admin}")
    private String adminUsername;

    @Value("${app.demo.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.demo.rrhh.username:rrhh}")
    private String rrhhUsername;

    @Value("${app.demo.rrhh.password:Rrhh123!}")
    private String rrhhPassword;

    @Value("${app.demo.employee.username:empleado}")
    private String employeeUsername;

    @Value("${app.demo.employee.password:Empleado123!}")
    private String employeePassword;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private DescuentoRepository descuentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureRoles();
        ensureDemoData();
    }

    private void ensureRoles() {
        createRoleIfMissing("ROLE_ADMIN", "Administrador general del sistema");
        createRoleIfMissing("ROLE_RRHH", "Gestor de planillas y reportes");
        createRoleIfMissing("ROLE_EMPLEADO", "Empleado con acceso a sus boletas");
    }

    private void ensureDemoData() {
        if (!bootstrapDemoData) {
            return;
        }

        Empleado primerEmpleado = empleadoRepository.findFirstAvailable()
                .orElseGet(this::createDefaultEmployee);

        createDiscountIfMissing("ISSS", 3.00);
        createDiscountIfMissing("AFP", 7.25);
        createDiscountIfMissing("RENTA", 0.00);

        createUserIfMissing(adminUsername, adminPassword, "ROLE_ADMIN", null);
        createUserIfMissing(rrhhUsername, rrhhPassword, "ROLE_RRHH", null);
        createUserIfMissing(employeeUsername, employeePassword, "ROLE_EMPLEADO", primerEmpleado);
    }

    private Empleado createDefaultEmployee() {
        Empleado empleado = new Empleado();
        empleado.setNombre("Andrea");
        empleado.setApellido("Méndez");
        empleado.setIdentificacion("01234567-8");
        empleado.setDireccion("San Salvador, El Salvador");
        empleado.setTipo("DOCENTE");
        empleado.setSalarioBaseVigente(720.00);
        return empleadoRepository.save(empleado);
    }

    private void createDiscountIfMissing(String tipo, Double porcentaje) {
        descuentoRepository.findTopByTipoIgnoreCaseOrderByVigenciaDesc(tipo).orElseGet(() -> {
            Descuento descuento = new Descuento();
            descuento.setTipo(tipo);
            descuento.setPorcentaje(porcentaje);
            descuento.setVigencia(LocalDate.now());
            return descuentoRepository.save(descuento);
        });
    }

    private void createRoleIfMissing(String nombreRol, String descripcion) {
        rolRepository.findByNombreRol(nombreRol).orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombreRol(nombreRol);
            rol.setDescripcion(descripcion);
            return rolRepository.save(rol);
        });
    }

    private void createUserIfMissing(String username, String rawPassword, String roleName, Empleado empleado) {
        if (usuarioRepository.existsByUsuarioIgnoreCase(username)) {
            return;
        }

        Rol rol = rolRepository.findByNombreRol(roleName)
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol base: " + roleName));

        Usuario usuario = new Usuario();
        usuario.setUsuario(username);
        usuario.setContrasena(passwordEncoder.encode(rawPassword));
        usuario.setRol(rol);
        usuario.setEmpleado(empleado);
        usuarioRepository.save(usuario);
    }
}
