package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest
class Html2PdfUtilTest {

	static final String HTMLFILEPATH = "/html/quotation.html"; // HTML file path

	static final String OUTPUTFILEPATH = "/result/quotation.pdf"; // 檔案輸出路徑

	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	@Autowired
	ResourceLoader resourceLoader;

	String htmlContent = ""; // html 內容

	ByteArrayOutputStream byteArrayOutputStream = null; // 轉換後的結果

	@BeforeEach
	void setUp() throws Exception {
		// 載入 HTML 內容
		this.htmlContent = new String(Files.readAllBytes(Paths.get(RESOURCE + HTMLFILEPATH)));

	}

	@Test
	void test() throws Exception {
		this.byteArrayOutputStream = Html2PdfUtil.convertHtmlToPdf(this.htmlContent);
		assertNotNull(this.byteArrayOutputStream);
	}

	@AfterEach
	void tearDown() throws Exception {
		// Create an output stream to write the PDF
		try (OutputStream outputStream = new FileOutputStream(RESOURCE + OUTPUTFILEPATH)) {
			outputStream.write(this.byteArrayOutputStream.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error");
		}

	}

}
