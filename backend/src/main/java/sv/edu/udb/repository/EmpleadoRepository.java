package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.Empleado;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    
    boolean existsByIdentificacion(String identificacion);

    @Query("SELECT e FROM Empleado e ORDER BY e.id_empleado ASC")
    List<Empleado> findPrimerEmpleado(Pageable pageable);

    default Optional<Empleado> findFirstAvailable() {
        return findPrimerEmpleado(PageRequest.of(0, 1)).stream().findFirst();
    }
}
