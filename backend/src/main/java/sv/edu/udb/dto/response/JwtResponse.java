package sv.edu.udb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String usuario;
    private Long empleadoId;
    private String nombreCompleto;
    private List<String> roles;
}
