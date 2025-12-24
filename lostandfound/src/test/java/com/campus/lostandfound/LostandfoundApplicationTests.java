package com.campus.lostandfound;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Application context loading test.
 * This test requires external dependencies (MySQL, Redis) to be available.
 * It is disabled by default and only runs when SPRING_INTEGRATION_TEST=true environment variable is set.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_INTEGRATION_TEST", matches = "true")
class LostandfoundApplicationTests {

	@Test
	void contextLoads() {
	}

}
