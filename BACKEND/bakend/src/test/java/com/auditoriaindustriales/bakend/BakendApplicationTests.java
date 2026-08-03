package com.auditoriaindustriales.bakend;

import org.junit.jupiter.api.Test;

/** Extiende IntegrationTestBase (perfil "test" + Postgres real vía Testcontainers): sin eso, no hay datasource contra el cual levantar el contexto. */
class BakendApplicationTests extends IntegrationTestBase {

	@Test
	void contextLoads() {
	}

}
