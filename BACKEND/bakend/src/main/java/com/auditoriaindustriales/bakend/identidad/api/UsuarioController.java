package com.auditoriaindustriales.bakend.identidad.api;

import com.auditoriaindustriales.bakend.identidad.api.dto.UsuarioCrearRequest;
import com.auditoriaindustriales.bakend.identidad.api.dto.UsuarioResponse;
import com.auditoriaindustriales.bakend.identidad.application.UsuarioService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Restringido a ADMIN/SUPERVISOR (lectura) y ADMIN (escritura) — ver SecurityConfig.RUTAS_USUARIOS_*. */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable UUID id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioCrearRequest request, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return usuarioService.crear(request, usuario.usuarioId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        usuarioService.desactivar(id, usuario.usuarioId());
    }
}
