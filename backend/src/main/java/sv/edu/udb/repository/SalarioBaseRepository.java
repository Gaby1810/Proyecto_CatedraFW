package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.SalarioBase;
import java.util.List;

@Repository
public interface SalarioBaseRepository extends JpaRepository<SalarioBase, Long> {

    // Cambiamos el método automático por uno manual para evitar el error del id_empleado
    @Query("SELECT s FROM SalarioBase s WHERE s.empleado.id_empleado = :idEmpleado ORDER BY s.fechaVigencia DESC")
    List<SalarioBase> findByEmpleadoId(@Param("idEmpleado") Long idEmpleado);
}