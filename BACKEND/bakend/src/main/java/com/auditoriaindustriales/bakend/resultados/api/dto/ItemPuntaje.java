package com.auditoriaindustriales.bakend.resultados.api.dto;

import java.math.BigDecimal;

/** Forma común para el ranking por subcategoría y el ranking por sección: mismo componente de barras en el front. evaluada=false + puntaje null es un hueco pendiente (plano cartesiano vacío), no un puntaje de cero. */
public record ItemPuntaje(String etiqueta, BigDecimal puntaje, String colorSemaforo, boolean evaluada) {
}
