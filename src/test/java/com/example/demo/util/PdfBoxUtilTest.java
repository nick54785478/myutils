package com.example.demo.util;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest
class PdfBoxUtilTest {

	Resource resource = null;

	@Autowired
	ResourceLoader resourceLoader;
	

	@BeforeEach
	void setUp() throws Exception {
		this.resource = resourceLoader.getResource("classpath:result/quotation.pdf");
	}

	@Test
	void testReadPdfContent() throws IOException {
		String pdfContent = PdfBoxUtil.readPdfContent(resource.getInputStream());
		System.out.println("pdf content: " + pdfContent);
	}

	@AfterEach
	void tearDown() throws Exception {

	}
}
