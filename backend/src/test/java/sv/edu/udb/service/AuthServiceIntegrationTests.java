package sv.edu.udb.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import sv.edu.udb.dto.request.LoginRequest;
import sv.edu.udb.dto.response.JwtResponse;
import sv.edu.udb.entity.Empleado;
import sv.edu.udb.entity.Rol;
import sv.edu.udb.entity.Usuario;
import sv.edu.udb.repository.EmpleadoRepository;
import sv.edu.udb.repository.RolRepository;
import sv.edu.udb.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class AuthServiceIntegrationTests {

    @Autowired
    private IAuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
        empleadoRepository.deleteAll();
        rolRepository.deleteAll();

        Rol rolEmpleado = new Rol();
        rolEmpleado.setNombreRol("ROLE_EMPLEADO");
        rolEmpleado.setDescripcion("Acceso para empleados");
        rolEmpleado = rolRepository.save(rolEmpleado);

        Empleado empleado = new Empleado();
        empleado.setNombre("Elena");
        empleado.setApellido("Martinez");
        empleado.setIdentificacion("12345678-9");
        empleado.setDireccion("San Salvador");
        empleado.setTipo("DOCENTE");
        empleado.setSalarioBaseVigente(950.00);
        empleado = empleadoRepository.save(empleado);

        Usuario usuario = new Usuario();
        usuario.setUsuario("empleado");
        usuario.setContrasena(passwordEncoder.encode("Empleado123!"));
        usuario.setRol(rolEmpleado);
        usuario.setEmpleado(empleado);
        usuarioRepository.save(usuario);
    }

    @Test
    void shouldAuthenticateEmployeeAndReturnProfileData() {
        LoginRequest request = new LoginRequest();
        request.setUsuario("empleado");
        request.setContrasena("Empleado123!");

        JwtResponse response = authService.login(request);

        assertNotNull(response.getToken());
        assertEquals("empleado", response.getUsuario());
        assertEquals("Elena Martinez", response.getNombreCompleto());
        assertEquals("ROLE_EMPLEADO", response.getRoles().get(0));
        assertNotNull(response.getEmpleadoId());
    }
}
