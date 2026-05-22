package sv.edu.udb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sv.edu.udb.entity.Descuento;

import java.util.Optional;

@Repository
public interface DescuentoRepository extends JpaRepository<Descuento, Long> {
    boolean existsByTipoIgnoreCase(String tipo);

    Optional<Descuento> findByTipoIgnoreCase(String tipo);

    Optional<Descuento> findTopByTipoIgnoreCaseOrderByVigenciaDesc(String tipo);
}
