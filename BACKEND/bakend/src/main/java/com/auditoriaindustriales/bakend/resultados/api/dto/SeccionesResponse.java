package com.auditoriaindustriales.bakend.resultados.api.dto;

import java.util.List;

public record SeccionesResponse(List<ItemPuntaje> items, int preguntasRespondidas, int preguntasEsperadas) {
}
