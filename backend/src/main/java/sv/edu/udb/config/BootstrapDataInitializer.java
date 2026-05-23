package sv.edu.udb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapDataInitializer.class);

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
        ensureAllEmployeesHaveAccess();
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

    /**
     * Crea automáticamente un acceso ROLE_EMPLEADO para cada empleado que aún
     * no tenga usuario vinculado.
     *
     * Username: primera parte del nombre sin tildes + punto + apellido sin tildes,
     *           todo en minúsculas y sin espacios (ej. "Gabriela Figueroa" → "gabriela.figueroa").
     *           Si el username ya está ocupado se usa "empleado{id}" como fallback.
     * Password por defecto: Empleado123!
     *
     * Es idempotente — se puede ejecutar múltiples veces sin duplicar registros.
     */
    private void ensureAllEmployeesHaveAccess() {
        if (!bootstrapDemoData) {
            return;
        }
        Rol rolEmpleado = rolRepository.findByNombreRol("ROLE_EMPLEADO")
                .orElseThrow(() -> new IllegalStateException("Rol ROLE_EMPLEADO no encontrado"));

        List<Empleado> todos = empleadoRepository.findAll();
        for (Empleado emp : todos) {
            if (usuarioRepository.existsByEmpleadoId(emp.getId_empleado())) {
                continue; // ya tiene acceso
            }
            String candidato = buildUsername(emp.getNombre(), emp.getApellido());
            if (usuarioRepository.existsByUsuarioIgnoreCase(candidato)) {
                candidato = "empleado" + emp.getId_empleado();
            }
            if (usuarioRepository.existsByUsuarioIgnoreCase(candidato)) {
                continue; // fallback también ocupado, no duplicar
            }
            Usuario usuario = new Usuario();
            usuario.setUsuario(candidato);
            usuario.setContrasena(passwordEncoder.encode("Empleado123!"));
            usuario.setRol(rolEmpleado);
            usuario.setEmpleado(emp);
            usuarioRepository.save(usuario);
            log.info("[Bootstrap] Usuario creado: {} → {} {} (id={})",
                    candidato, emp.getNombre(), emp.getApellido(), emp.getId_empleado());
        }
    }

    /** Convierte "Gabriela Figueroa" → "gabriela.figueroa" (sin tildes, sin espacios). */
    private String buildUsername(String nombre, String apellido) {
        String n = stripAccents(nombre.trim().split("\\s+")[0]).toLowerCase();
        String a = stripAccents(apellido.trim().split("\\s+")[0]).toLowerCase();
        return n + "." + a;
    }

    private String stripAccents(String s) {
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        return normalized.replaceAll("[^\\p{ASCII}]", "").replaceAll("[^a-zA-Z0-9]", "");
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
