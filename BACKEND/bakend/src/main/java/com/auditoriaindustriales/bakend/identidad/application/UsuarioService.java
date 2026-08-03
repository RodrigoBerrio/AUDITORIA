package com.auditoriaindustriales.bakend.identidad.application;

import com.auditoriaindustriales.bakend.identidad.api.UsuarioMapper;
import com.auditoriaindustriales.bakend.identidad.api.dto.UsuarioCrearRequest;
import com.auditoriaindustriales.bakend.identidad.api.dto.UsuarioResponse;
import com.auditoriaindustriales.bakend.identidad.domain.Usuario;
import com.auditoriaindustriales.bakend.identidad.domain.UsuarioRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Alta y gestión de usuarios reales del sistema (auditor/admin/supervisor) — no hay registro público, solo ADMIN puede crear. */
@Service
@Transactional
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.listar().stream().map(usuarioMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(UUID id) {
        return usuarioMapper.toResponse(buscarOFallar(id));
    }

    public UsuarioResponse crear(UsuarioCrearRequest request, UUID creadoPor) {
        if (usuarioRepository.existePorCorreo(request.correo())) {
            throw new ConflictException("Ya existe un usuario con el correo " + request.correo() + ".");
        }
        Usuario usuario = Usuario.crear(request.nombre(), request.correo(), passwordEncoder.encode(request.password()), request.rol());
        Usuario guardado = usuarioRepository.guardar(usuario);
        log.info("usuario={} (rol={}) creado por admin={}", guardado.getId(), guardado.getRol(), creadoPor);
        return usuarioMapper.toResponse(guardado);
    }

    public void desactivar(UUID id, UUID desactivadoPor) {
        Usuario usuario = buscarOFallar(id);
        usuario.desactivar();
        usuarioRepository.guardar(usuario);
        log.info("usuario={} desactivado por admin={}", id, desactivadoPor);
    }

    private Usuario buscarOFallar(UUID id) {
        return usuarioRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Usuario", id));
    }
}
