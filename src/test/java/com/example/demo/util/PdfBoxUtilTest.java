package com.example.demo.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

// PDF 來源: https://blog.fileformat.com/file-formats/download-sample-files/?utm_source=chatgpt.com
@SpringBootTest
class PdfBoxUtilTest {

	@Autowired
	ResourceLoader resourceLoader;

	@Test
	void testReadPdfContent() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/quotation.pdf");
		String pdfContent = PdfBoxUtil.readPdfContent(resource.getInputStream());
		System.out.println("pdf content: " + pdfContent);
		assertTrue(StringUtils.isNotBlank(pdfContent));
	}

	@Test
	void testExtractTable() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/SamplePDF-19kb-Text-Formatting-1Page.pdf");
		List<List<String>> tableData = PdfBoxUtil.extractTable(resource.getInputStream(), 1, 5, 50);
		System.out.println("pdf content: " + tableData);
		assertTrue(!tableData.isEmpty());
	}
	
	@AfterEach
	void tearDown() throws Exception {

	}
}
