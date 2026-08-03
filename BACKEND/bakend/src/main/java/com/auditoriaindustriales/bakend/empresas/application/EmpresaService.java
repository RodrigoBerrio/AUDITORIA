package com.auditoriaindustriales.bakend.empresas.application;

import com.auditoriaindustriales.bakend.empresas.api.EmpresaMapper;
import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaRequest;
import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaResponse;
import com.auditoriaindustriales.bakend.empresas.domain.Empresa;
import com.auditoriaindustriales.bakend.empresas.domain.EmpresaRepository;
import com.auditoriaindustriales.bakend.empresas.infrastructure.EmpresaJpaRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    // Solo para la proyección de listado (auditoriasRealizadas/ultimaVisita);
    // ver comentario en EmpresaJpaRepository sobre por qué es SQL nativo.
    private final EmpresaJpaRepository empresaJpaRepository;
    private final EmpresaMapper empresaMapper;

    public EmpresaService(EmpresaRepository empresaRepository, EmpresaJpaRepository empresaJpaRepository, EmpresaMapper empresaMapper) {
        this.empresaRepository = empresaRepository;
        this.empresaJpaRepository = empresaJpaRepository;
        this.empresaMapper = empresaMapper;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listar() {
        return empresaJpaRepository.listarConMetricas().stream().map(empresaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponse obtener(UUID id) {
        return empresaJpaRepository.buscarConMetricas(id)
                .map(empresaMapper::toResponse)
                .orElseThrow(() -> NotFoundException.of("Empresa", id));
    }

    public EmpresaResponse crear(EmpresaRequest request) {
        if (empresaRepository.existePorNit(request.nit())) {
            throw new ConflictException("Ya existe una empresa registrada con el NIT " + request.nit() + ".");
        }
        Empresa empresa = Empresa.crear(request.razonSocial(), request.nit());
        empresa.actualizarDatos(
                request.razonSocial(), request.sector(), request.numEmpleados(), request.ciudad(),
                request.departamento(), request.codigoPostal(), request.contacto(), request.telefono(),
                request.correo(), request.descripcion());
        return empresaMapper.toResponseNueva(empresaRepository.guardar(empresa));
    }

    public EmpresaResponse actualizar(UUID id, EmpresaRequest request) {
        Empresa empresa = empresaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Empresa", id));
        empresa.actualizarDatos(
                request.razonSocial(), request.sector(), request.numEmpleados(), request.ciudad(),
                request.departamento(), request.codigoPostal(), request.contacto(), request.telefono(),
                request.correo(), request.descripcion());
        empresaRepository.guardar(empresa);
        return obtener(id);
    }

    public void eliminar(UUID id) {
        Empresa empresa = empresaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Empresa", id));
        empresaRepository.eliminar(empresa);
    }
}
