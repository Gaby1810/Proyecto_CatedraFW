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

    @Query("SELECT COALESCE(SUM(p.salarioBruto), 0)    FROM Planilla p")
    Double sumSalarioBruto();

    @Query("SELECT COALESCE(SUM(p.totalDescuentos), 0) FROM Planilla p")
    Double sumTotalDescuentos();

    @Query("SELECT COALESCE(SUM(p.salarioNeto), 0)     FROM Planilla p")
    Double sumSalarioNeto();

    @Query("SELECT DISTINCT p.periodo FROM Planilla p ORDER BY p.periodo DESC")
    List<String> findDistinctPeriodos();

    /** [periodo, cantidad, sumNeto] agrupado por período, más reciente primero. */
    @Query("SELECT p.periodo, COUNT(p), SUM(p.salarioNeto) FROM Planilla p GROUP BY p.periodo ORDER BY p.periodo DESC")
    List<Object[]> findResumenPorPeriodo();

    @Query("SELECT p FROM Planilla p ORDER BY p.id_planilla DESC")
    List<Planilla> findTop6OrderByIdDesc(org.springframework.data.domain.Pageable pageable);

    default List<Planilla> findUltimas6() {
        return findTop6OrderByIdDesc(org.springframework.data.domain.PageRequest.of(0, 6));
    }
}
