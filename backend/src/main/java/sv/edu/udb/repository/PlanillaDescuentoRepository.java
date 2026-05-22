package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.PlanillaDescuento;

import java.util.List;

@Repository
public interface PlanillaDescuentoRepository extends JpaRepository<PlanillaDescuento, Long> {
    @Query("SELECT pd FROM PlanillaDescuento pd WHERE pd.planilla.id_planilla = :idPlanilla")
    List<PlanillaDescuento> buscarPorIdPlanilla(@Param("idPlanilla") Long idPlanilla);
}
