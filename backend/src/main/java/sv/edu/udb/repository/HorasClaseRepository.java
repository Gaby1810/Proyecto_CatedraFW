package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.HorasClase;

import java.util.Optional;

@Repository
public interface HorasClaseRepository extends JpaRepository<HorasClase, Long> {

    @Query("SELECT h FROM HorasClase h WHERE h.empleado.id_empleado = :idEmpleado AND h.periodo = :periodo")
    Optional<HorasClase> findByEmpleadoAndPeriodo(@Param("idEmpleado") Long idEmpleado, @Param("periodo") String periodo);
}
