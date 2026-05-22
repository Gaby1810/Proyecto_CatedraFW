package sv.edu.udb.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.dto.request.LoginRequest;
import sv.edu.udb.dto.response.JwtResponse;
import sv.edu.udb.dto.response.UserProfileResponse;
import sv.edu.udb.projection.AuthProfileView;
import sv.edu.udb.repository.UsuarioRepository;
import sv.edu.udb.security.JwtTokenProvider;
import sv.edu.udb.service.IAuthService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsuario(), loginRequest.getContrasena())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Usuario o contraseña inválidos");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String jwt = jwtTokenProvider.generateToken(authentication);
      
        org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        AuthProfileView usuario = usuarioRepository.findAuthProfileView(userDetails.getUsername())
                .orElseThrow(() -> new BadCredentialsException("No fue posible cargar el perfil del usuario autenticado"));

        String nombreCompleto = usuario.getEmpleadoId() == null
                ? "Usuario del sistema"
                : usuario.getNombre() + " " + usuario.getApellido();

        return new JwtResponse(
                jwt,
                "Bearer",
                usuario.getUserId(),
                userDetails.getUsername(),
                usuario.getEmpleadoId(),
                nombreCompleto,
                roles
        );
    }

    @Override
    public UserProfileResponse getAuthenticatedProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new BadCredentialsException("No hay una sesión autenticada");
        }

        AuthProfileView usuario = usuarioRepository.findAuthProfileView(authentication.getName())
                .orElseThrow(() -> new BadCredentialsException("No fue posible cargar el perfil del usuario"));

        UserProfileResponse response = new UserProfileResponse();
        response.setUserId(usuario.getUserId());
        response.setUsuario(usuario.getUsuario());
        response.setEmpleadoId(usuario.getEmpleadoId());
        response.setNombreCompleto(usuario.getEmpleadoId() == null
                ? "Usuario del sistema"
                : usuario.getNombre() + " " + usuario.getApellido());
        response.setRoles(List.of(usuario.getRoleName()));
        return response;
    }
}
