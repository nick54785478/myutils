package com.example.demo.util;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FreeMarkerTemplateGenerateUtilTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testProcessTemplateInputStreamStringMapOfStringObject() {
		InputStream inputStream = FreeMarkerTemplateGenerateUtilTest.class
				.getResourceAsStream("/freemarker/notify-template.html");

		Map<String, Object> model = Map.of(
			    "recipientName", "John Doe",
			    "formNo", "PSB52025120141",
			    "url", "https://sqm.example.com/8d/PSB52025120141"
			);

		String html = FreeMarkerTemplateGenerateUtil.processTemplate(inputStream, "notify-template.html", model);
		System.out.println(html);
		assertNotNull(html);
	}

}
