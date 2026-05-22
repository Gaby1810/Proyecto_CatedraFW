package sv.edu.udb.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sv.edu.udb.entity.Planilla;
import sv.edu.udb.projection.AuthProfileView;
import sv.edu.udb.repository.UsuarioRepository;

@Service
public class CurrentUserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AuthProfileView getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("No hay un usuario autenticado");
        }

        return usuarioRepository.findAuthProfileView(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario autenticado no encontrado"));
    }

    public boolean hasRole(String roleName) {
        return roleName.equalsIgnoreCase(getCurrentUser().getRoleName());
    }

    public void validateEmployeeAccess(Long empleadoId) {
        if (hasRole("ROLE_ADMIN") || hasRole("ROLE_RRHH")) {
            return;
        }

        AuthProfileView usuario = getCurrentUser();
        if (usuario.getEmpleadoId() == null || !empleadoId.equals(usuario.getEmpleadoId())) {
            throw new AccessDeniedException("No tienes permisos para consultar la información de otro empleado");
        }
    }

    public void validatePlanillaAccess(Planilla planilla) {
        if (planilla.getEmpleado() == null) {
            throw new AccessDeniedException("La planilla no está asociada a un empleado válido");
        }
        validateEmployeeAccess(planilla.getEmpleado().getId_empleado());
    }
}
