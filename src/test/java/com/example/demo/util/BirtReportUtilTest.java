package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class BirtReportUtilTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void testGeneratePdfReportStringStringMapOfStringObjectMapOfStringListOfMapOfStringObject() {
		InputStream inputStream = BirtReportUtil.getResourceInputStream("birt/report", "User.rptdesign");
		assertNotNull(inputStream);
	}

	@Test
	void testGeneratePdfReportInputStreamMapOfStringObjectMapOfStringListOfMapOfStringObject() {

		InputStream inputStream = BirtReportUtil.getResourceInputStream("birt/report", "User.rptdesign");
		Map<String, Object> params = new HashMap<>();
		params.put("teacher", "孔老二");
		params.put("referenceNo", "ref20250728");

		List<Map<String, Object>> dataList = getMockDataList();
		Map<String, Object> dataContext = Map.of("dataList", dataList);

		ByteArrayResource resource = BirtReportUtil.generatePdfReport(inputStream, params, dataContext);

		assertNotNull(resource);
		try {
			this.downloadLocally(resource, "student.pdf");
		} catch (IOException e) {
			log.error("發生錯誤，下載檔案失敗");
		}
	}

	@Test
	void testInsertImage() throws IOException {
		ClassPathResource classPathResource = new ClassPathResource("birt/image/zhaoyun.jpg");

		// 將 InputStream 轉換為 Base64 字串
		String imageString = Base64Util.encode(classPathResource.getInputStream());
		Map<String, Object> params = new HashMap<>();

		// 設置進 Parameter
		params.put("image", imageString);

		// 取得 BIRT 範本資料流
		InputStream inputStream = BirtReportUtil.getResourceInputStream("birt/report", "Zhaoyun.rptdesign");

		ByteArrayResource resource = BirtReportUtil.generatePdfReport(inputStream, params, Map.of());
		assertNotNull(resource);
		try {
			this.downloadLocally(resource, "imagePdf.pdf");
		} catch (IOException e) {
			log.error("發生錯誤，下載檔案失敗");
		}
	}

	@Test
	void testLoadJpg() throws IOException {
		ClassPathResource resource = new ClassPathResource("birt/image/zhaoyun.jpg");
		assertTrue(resource.exists());
		try (InputStream is = resource.getInputStream()) {
			assertNotNull(is);
			System.out.println("JPG size = " + is.available());
		}
	}

	/**
	 * 本地端下載
	 */
	private void downloadLocally(ByteArrayResource resource, String filename) throws IOException {
		String outputPath = System.getProperty("user.dir") + "/src/main/resources" + "/birt/result";
		// 從ByteArrayResource中讀取內容並寫入OutputStream
		try (FileOutputStream fos = new FileOutputStream(outputPath + "/" + filename)) {
			fos.write(resource.getContentAsByteArray());
		}
	}

	/**
	 * 生成 DataList 模擬數據
	 */
	private static List<Map<String, Object>> getMockDataList() {
		List<Map<String, Object>> dataList = new ArrayList<>();
		for (int i = 1; i <= 45; i++) {
			Map<String, Object> map = new HashMap<>();
			int chineseScore = (int) (Math.random() * 100); // 隨機取數
			int mathScore = (int) (Math.random() * 100); // 隨機取數
			int englishScore = (int) (Math.random() * 100); // 隨機取數

			map.put("number", i);
			map.put("name", UUID.randomUUID().toString().substring(0, 6)); // 截前六碼

			map.put("chineseScore", chineseScore);
			map.put("mathScore", mathScore);
			map.put("englishScore", englishScore);
			map.put("testTime", new Date());
			dataList.add(map);
		}
		return dataList;
	}

}
