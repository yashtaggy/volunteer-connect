package com.volunteerconnect.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

// Configure the test to load a full Spring Boot application context.
// WebEnvironment.RANDOM_PORT starts the application on a random available port,
// which is good for integration tests and avoids port conflicts.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // This annotation configures MockMvc for testing HTTP endpoints
class BackendApplicationTests {

	// We can now inject MockMvc to perform mock HTTP requests in our tests
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
		// This test simply verifies that the Spring application context loads successfully.
		// With security enabled, this often requires configuring @SpringBootTest
		// correctly to handle the security setup. If this passes, the context loaded.
		// If this test passes, it means your application context (including security) initializes correctly.
	}
}