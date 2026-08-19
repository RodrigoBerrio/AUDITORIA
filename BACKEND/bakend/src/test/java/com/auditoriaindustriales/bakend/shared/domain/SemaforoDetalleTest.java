package com.auditoriaindustriales.bakend.shared.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica los umbrales exactos de la escala de 4 niveles por criterio/dimensión — distinta del semáforo único de 3 niveles (Semaforo), que sigue gobernando subcategoría/categoría/auditoría completa. */
class SemaforoDetalleTest {

    @Test
    void justoDebajoDeDosSigueEnCritico() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("1.99"))).isEqualTo(SemaforoDetalle.CRITICO);
    }

    @Test
    void enElUmbralDeDosPasaASerio() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("2.00"))).isEqualTo(SemaforoDetalle.SERIO);
    }

    @Test
    void justoDebajoDeTresSigueEnSerio() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("2.99"))).isEqualTo(SemaforoDetalle.SERIO);
    }

    @Test
    void enElUmbralDeTresPasaAAlerta() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("3.00"))).isEqualTo(SemaforoDetalle.ALERTA);
    }

    @Test
    void justoDebajoDeCuatroSigueEnAlerta() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("3.99"))).isEqualTo(SemaforoDetalle.ALERTA);
    }

    @Test
    void enElUmbralDeCuatroPasaABueno() {
        assertThat(SemaforoDetalle.desde(new BigDecimal("4.00"))).isEqualTo(SemaforoDetalle.BUENO);
    }

    @Test
    void puntajeNuloSeTrataComoSinDatosNoComoCritico() {
        assertThat(SemaforoDetalle.desde(null)).isEqualTo(SemaforoDetalle.SIN_DATOS);
    }
}
