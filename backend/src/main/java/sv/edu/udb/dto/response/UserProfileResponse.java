package sv.edu.udb.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class UserProfileResponse {
    private Long userId;
    private String usuario;
    private Long empleadoId;
    private String nombreCompleto;
    private List<String> roles;
}
