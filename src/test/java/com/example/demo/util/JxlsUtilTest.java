package com.example.demo.util;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootTest
class JxlsUtilTest {
	
	@Autowired
	ResourceLoader resourceLoader;
	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	Resource resource = null;
	
	Map<String, Object> map = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		this.resource = resourceLoader.getResource("classpath:/excel/Quotation.xlsx");
		map.put("userEmail", "hello@wpgholdings.com");
		map.put("userEname", "Happy Wang");
		map.put("userName", "王開心");
	}

	@Test
	void test() throws IOException {
		try (OutputStream outputStream = new FileOutputStream(new File(RESOURCE + "/result/quotation.xlsx"))) {

			JxlsUtil.exportExcel(this.resource.getInputStream(), outputStream, map);
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error");
		}
	}

	@AfterEach
	void tearDown() throws Exception {
	}

}
