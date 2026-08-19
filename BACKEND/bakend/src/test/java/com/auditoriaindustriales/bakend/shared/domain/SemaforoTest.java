package com.auditoriaindustriales.bakend.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica los umbrales exactos del semáforo único (regla no negociable del spec de resultados gráficos). */
class SemaforoTest {

    @Test
    void justoDebajoDeCriticoSigueEnCritico() {
        assertThat(Semaforo.desde(new BigDecimal("2.49"))).isEqualTo(Semaforo.CRITICO);
    }

    @Test
    void enElUmbralDeDosPuntoCincoPasaAEnDesarrollo() {
        assertThat(Semaforo.desde(new BigDecimal("2.50"))).isEqualTo(Semaforo.EN_DESARROLLO);
    }

    @Test
    void justoDebajoDeTresPuntoSetentaYCincoSigueEnDesarrollo() {
        assertThat(Semaforo.desde(new BigDecimal("3.74"))).isEqualTo(Semaforo.EN_DESARROLLO);
    }

    @Test
    void enElUmbralDeTresPuntoSetentaYCincoPasaAConsolidado() {
        assertThat(Semaforo.desde(new BigDecimal("3.75"))).isEqualTo(Semaforo.CONSOLIDADO);
    }

    @Test
    void puntajeNuloSeTrataComoSinDatosNoComoCritico() {
        assertThat(Semaforo.desde(null)).isEqualTo(Semaforo.SIN_DATOS);
    }
}
