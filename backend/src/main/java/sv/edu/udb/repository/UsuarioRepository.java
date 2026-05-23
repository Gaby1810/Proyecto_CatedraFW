package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.Usuario;
import sv.edu.udb.projection.AuthProfileView;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);

    Optional<Usuario> findByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.empleado.id_empleado = :idEmpleado")
    boolean existsByEmpleadoId(@Param("idEmpleado") Long idEmpleado);

    @Query("""
            SELECT u
            FROM Usuario u
            LEFT JOIN FETCH u.empleado
            JOIN FETCH u.rol
            WHERE LOWER(u.usuario) = LOWER(:usuario)
            """)
    Optional<Usuario> findForAuthentication(@Param("usuario") String usuario);

    @Query("""
            SELECT
                u.id_usuario AS userId,
                u.usuario AS usuario,
                e.id_empleado AS empleadoId,
                e.nombre AS nombre,
                e.apellido AS apellido,
                r.nombreRol AS roleName
            FROM Usuario u
            JOIN u.rol r
            LEFT JOIN u.empleado e
            WHERE LOWER(u.usuario) = LOWER(:usuario)
            """)
    Optional<AuthProfileView> findAuthProfileView(@Param("usuario") String usuario);
}
