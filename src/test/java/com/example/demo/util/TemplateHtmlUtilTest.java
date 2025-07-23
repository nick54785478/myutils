package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TemplateHtmlUtilTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testReadHtmlFileStringString() throws IOException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("username", "Nick");
		String email = TemplateHtmlUtil.readHtmlFile("/email", "email-template.html");
		assertNotNull(email);
	}

	@Test
	void testGenerateStandardHtmlContent() throws IOException {
		HashMap<String, Object> params = new HashMap<>();
		params.put("username", "Nick");
		String standardHtmlContent = TemplateHtmlUtil.generateStandardHtmlContent("/email", "email-template.html",
				params);
		assertNotNull(standardHtmlContent);
	}

	@Test
	void testGetResource() {
		InputStream resource = TemplateHtmlUtil.getResource("/email", "email-template.html");
		assertNotNull(resource);
	}

}
