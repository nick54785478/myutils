package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest
class PdfBoxImageUtilTest {

	@Autowired
	ResourceLoader resourceLoader;

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testExtractFirstImage() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/sample-pdf-with-images.pdf");
		byte[] pdfBytes = resource.getInputStream().readAllBytes();
		InputStreamResource firstImage = PdfBoxImageUtil.extractFirstImage(pdfBytes);
		this.downloadLocally(new ByteArrayResource(firstImage.getInputStream().readAllBytes()), "/image.png");
		assertNotNull(firstImage);
	}

	@Test
	void testExtractFirstImageAsBase64() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/sample-pdf-with-images.pdf");
		byte[] pdfBytes = resource.getInputStream().readAllBytes();
		String firstImageAsBase64 = PdfBoxImageUtil.extractFirstImageAsBase64(pdfBytes);
		System.out.println(firstImageAsBase64);
		assertNotNull(firstImageAsBase64);
	}

	@Test
	void testExtractAllImagesAsZip() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/sample-pdf-with-images.pdf");
		byte[] pdfBytes = resource.getInputStream().readAllBytes();
		InputStreamResource inputStreamResource = PdfBoxImageUtil.extractAllImagesAsZip(pdfBytes);
		this.downloadLocally(new ByteArrayResource(inputStreamResource.getInputStream().readAllBytes()), "/images.zip");
		assertNotNull(inputStreamResource);
	}

	@Test
	void testExtractAllImagesAsZipBase64() throws IOException {
		Resource resource = resourceLoader.getResource("classpath:pdfbox/sample-pdf-with-images.pdf");
		byte[] pdfBytes = resource.getInputStream().readAllBytes();
		String imageAsBase64 = PdfBoxImageUtil.extractAllImagesAsZipBase64(pdfBytes);
		System.out.println(imageAsBase64);
		assertNotNull(imageAsBase64);
	}

	/**
	 * 本地端下載
	 */
	private void downloadLocally(ByteArrayResource resource, String fileName) throws IOException {
		String outputPath = System.getProperty("user.dir") + "/src/main/resources" + "/pdfbox/result";
		// 從ByteArrayResource中讀取內容並寫入OutputStream
		try (FileOutputStream fos = new FileOutputStream(outputPath + fileName)) {
			fos.write(resource.getContentAsByteArray());
		}
	}

}
