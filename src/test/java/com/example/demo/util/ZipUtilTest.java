package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

class ZipUtilTest {

	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	@BeforeEach
	void setUp() throws Exception {

	}

	@Test
	void testPackFileToZipFromBytes() throws IOException {
		// 正確地從資源中取得檔案 InputStream
		try (InputStream input = ZipUtil.class.getResourceAsStream("/zip/quotation1.pdf")) {
			if (input == null) {
				System.out.println("沒取到資料");
				return;
			}

			byte[] fileBytes = input.readAllBytes(); // 讀取檔案內容為 byte[]
			String fileName = "quotation1.pdf"; // 放入 zip 中的檔名（不要帶路徑）

			ByteArrayResource resource = ZipUtil.packFileToZipFromBytes(fileBytes, fileName);
			// 本地下載
			String zipName = RESOURCE + "/zip/result/quotation_from_byte.zip";
			this.downloadLocally(resource, zipName);
			assertNotNull(resource);
		}

	}

	@Test
	void testPackFileToZipFromBytesWithPassword() throws IOException {
		try (InputStream input = ZipUtil.class.getResourceAsStream("/zip/quotation1.pdf")) {
			if (input == null) {
				System.out.println("沒取到資料");
				return;
			}
			byte[] fileBytes = input.readAllBytes(); // 讀取檔案內容為 byte[]
			String fileName = "quotation1.pdf"; // 放入 zip 中的檔名（不要帶路徑）
			ByteArrayResource resource = ZipUtil.packFileToZipFromBytes(fileBytes, fileName, "password");
			// 本地下載
			String zipName = RESOURCE + "/zip/result/quotation_from_byte_with_password.zip";
			this.downloadLocally(resource, zipName);
			assertNotNull(resource);
		}
	}

	@Test
	void testPackMultiFileToZipFromInputStream() throws IOException {
		InputStream resourceAsStream = ZipUtil.class.getResourceAsStream("/zip/quotation1.pdf");
		InputStream resourceAsStream2 = ZipUtil.class.getResourceAsStream("/zip/quotation2.pdf");
		Map<String, InputStream> map = Map.of("quotation1.pdf", resourceAsStream, "quotation2.pdf", resourceAsStream2);
		Resource resource = ZipUtil.packFilesToZipFromInputStream(map);
		// 本地下載
		String zipName = RESOURCE + "/zip/result/quotations_from_inputStream.zip";
		this.downloadLocally(resource, zipName);
		assertNotNull(resource);
	}

	@Test
	void testPackMultiFileToZipFromInputStreamWithPassword() throws IOException {
		InputStream resourceAsStream = ZipUtil.class.getResourceAsStream("/zip/quotation1.pdf");
		InputStream resourceAsStream2 = ZipUtil.class.getResourceAsStream("/zip/quotation2.pdf");
		Map<String, InputStream> map = Map.of("quotation1.pdf", resourceAsStream, "quotation2.pdf", resourceAsStream2);
		Resource resource = ZipUtil.packFilesToZipFromInputStream(map, "password");
		// 本地下載
		String zipName = RESOURCE + "/zip/result/quotations_from_inputStream_with_password.zip";
		this.downloadLocally(resource, zipName);
		assertNotNull(resource);
	}

	private void downloadLocally(Resource resource, String zipName) throws IOException {
		// 本地端下載
		// 從ByteArrayResource中讀取內容並寫入OutputStream
		try (FileOutputStream fos = new FileOutputStream(zipName)) {
			fos.write(resource.getContentAsByteArray());
		}
	}

	@AfterEach
	void tearDown() throws Exception {

	}
}