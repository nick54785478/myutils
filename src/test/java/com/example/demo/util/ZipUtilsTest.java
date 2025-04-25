package com.example.demo.util;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest
class ZipUtilsTest {

	@Autowired
	ResourceLoader resourceLoader;

	static final String OUTPUTFILEPATH = System.getProperty("user.dir") + "/src/main/resources" + "/result";
	// 檔案輸出路徑

	byte[] pdfFile = null; // pdf 文件
	byte[] pdfFile2 = null; // pdf 文件二號

	@BeforeEach
	void setUp() throws Exception {
		// 取得第一個檔案 quotation.pdf
		Resource resource = resourceLoader.getResource("classpath:result/quotation.pdf");
		this.pdfFile = resource.getContentAsByteArray();
		
		// 第二個檔案 quotation2.pdf
		Resource resource2 = resourceLoader.getResource("classpath:result/quotation2.pdf");
		this.pdfFile2 = resource2.getContentAsByteArray();
	}

	@Test
	void testPackFileToZip() throws IOException {
		String fileName = "quotation.pdf";
		String zipName = OUTPUTFILEPATH + "/quotation.zip";

		ZipUtils.packToZip(this.pdfFile, fileName, zipName);
	}
	
	@Test
	void testPackMultiFileToZip() throws IOException {
		String fileName = "quotation.pdf";
		String fileName2 = "quotation2.pdf";
		String zipName = OUTPUTFILEPATH + "/multi-quotation.zip";
		
		Map<String, byte[]> map = Map.of(fileName, this.pdfFile, fileName2, this.pdfFile2);
		
		ZipUtils.packToZip(map, zipName);

	}

	@AfterEach
	void tearDown() throws Exception {

	}
}
