package com.auditoriaindustriales.bakend.resultados.api.dto;

import java.util.List;

public record RankingResponse(List<ItemPuntaje> items, int preguntasRespondidas, int preguntasEsperadas) {
}
