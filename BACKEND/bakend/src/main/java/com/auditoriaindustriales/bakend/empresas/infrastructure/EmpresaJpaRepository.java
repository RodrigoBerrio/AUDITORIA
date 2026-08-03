package com.auditoriaindustriales.bakend.empresas.infrastructure;

import com.auditoriaindustriales.bakend.empresas.domain.Empresa;
import com.auditoriaindustriales.bakend.empresas.domain.EmpresaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaJpaRepository extends JpaRepository<Empresa, UUID>, EmpresaRepository {

    boolean existsByNit(String nit);

    @Override
    default Optional<Empresa> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default boolean existePorNit(String nit) {
        return existsByNit(nit);
    }

    @Override
    default Empresa guardar(Empresa empresa) {
        return save(empresa);
    }

    @Override
    default void eliminar(Empresa empresa) {
        delete(empresa);
    }

    String PROYECCION = """
            select
                e.id                as id,
                e.razon_social      as razonSocial,
                e.nit               as nit,
                e.sector            as sector,
                e.num_empleados     as numEmpleados,
                e.ciudad            as ciudad,
                e.departamento      as departamento,
                e.codigo_postal     as codigoPostal,
                e.contacto          as contacto,
                e.telefono          as telefono,
                e.correo            as correo,
                e.descripcion       as descripcion,
                count(a.id)         as auditoriasRealizadas,
                max(coalesce(a.fecha_fin, a.fecha_inicio)) as ultimaVisita
            from empresa e
            left join auditoria a on a.empresa_id = e.id
            """;

    @Query(value = PROYECCION + " group by e.id order by e.razon_social", nativeQuery = true)
    List<EmpresaListadoRow> listarConMetricas();

    @Query(value = PROYECCION + " where e.id = :id group by e.id", nativeQuery = true)
    Optional<EmpresaListadoRow> buscarConMetricas(@Param("id") UUID id);
}
