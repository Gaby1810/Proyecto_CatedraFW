package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.Planilla;
import java.util.List;

@Repository
public interface PlanillaRepository extends JpaRepository<Planilla, Long> {
    
    @Query("SELECT p FROM Planilla p WHERE p.empleado.id_empleado = :idEmpleado")
    List<Planilla> buscarPorIdEmpleado(@Param("idEmpleado") Long idEmpleado);

    List<Planilla> findByPeriodo(String periodo);

    @Query("SELECT COUNT(p) > 0 FROM Planilla p WHERE p.empleado.id_empleado = :idEmpleado AND p.periodo = :periodo")
    boolean existsForEmployeeAndPeriod(@Param("idEmpleado") Long idEmpleado, @Param("periodo") String periodo);
}
