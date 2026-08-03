package com.auditoriaindustriales.bakend.reportes.infrastructure;

import com.auditoriaindustriales.bakend.reportes.domain.Reporte;
import com.auditoriaindustriales.bakend.reportes.domain.ReporteRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReporteJpaRepository extends JpaRepository<Reporte, UUID>, ReporteRepository {

    List<Reporte> findByAuditoriaIdOrderByGeneradoEnDesc(UUID auditoriaId);

    @Override
    default Optional<Reporte> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Reporte> listarPorAuditoria(UUID auditoriaId) {
        return findByAuditoriaIdOrderByGeneradoEnDesc(auditoriaId);
    }

    @Override
    default Reporte guardar(Reporte reporte) {
        return save(reporte);
    }
}
