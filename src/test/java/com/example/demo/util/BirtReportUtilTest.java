package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

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

		Map<String, Object> context1 = new HashMap<>();
		context1.put("number", 1);
		context1.put("name", "王小明");
		context1.put("score", 100);
		context1.put("testTime", new Date());

		Map<String, Object> context2 = new HashMap<>();
		context2.put("number", 2);
		context2.put("name", "李大牛");
		context2.put("score", 100);
		context2.put("testTime", new Date());

		List<Map<String, Object>> dataList = List.of(context1, context2);
		Map<String, List<Map<String, Object>>> dataContext = Map.of("dataList", dataList);

		ByteArrayResource resource = BirtReportUtil.generatePdfReport(inputStream, params, dataContext);
		
		assertNotNull(resource);
		try {
			this.downloadLocally(resource);
		} catch (IOException e) {
			log.error("發生錯誤，下載檔案失敗");
		}
	}

	/**
	 * 本地端下載
	 */
	private void downloadLocally(ByteArrayResource resource) throws IOException {
		String outputPath = System.getProperty("user.dir") + "/src/main/resources" + "/birt/result";
		// 從ByteArrayResource中讀取內容並寫入OutputStream
		try (FileOutputStream fos = new FileOutputStream(outputPath + "/student.pdf")) {
			fos.write(resource.getContentAsByteArray());
		}
	}

}
