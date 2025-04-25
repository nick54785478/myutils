package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.model.employee.Employee;

@SpringBootTest
class ExcelUtilTest {

	static XSSFWorkbook book = null;
	List<String> headers = new ArrayList<>();
	List<Employee> employees = new ArrayList<>();

	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	/**
	 * 資料預先準備
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.headers = List.of("姓名", "員編", "英文名稱", "年齡", "信箱");
		this.employees = List.of(new Employee(1L, "王小明", "Ming", 29, "Ming123@example.com"),
				new Employee(2L, "張三", "Ray", 26, "ray@example.com"),
				new Employee(3L, "李四", "Jay", 26, "jay@example.com"));

	}

	/**
	 * 寫入 Excel 檔
	 */
	@Test
	void test() {
		ExcelUtilTest.book = ExcelUtil.processWorkbook(this.headers, this.employees);
	}

	/**
	 * 讀取 Excel 檔
	 * 
	 * @throws IOException
	 */
	@Test
	void testReadExcelData() throws IOException {
		byte[] byteArray = ExcelUtil.exportDataAsByteArray(headers, employees);
		List<Map<String, String>> excelData = ExcelUtil.readExcelData(new ByteArrayInputStream(byteArray), "Books");
		System.out.println("excelData: " + excelData);
	}

	/**
	 * 本地端進行下載
	 */
	@AfterAll
	static void tearDown() throws Exception {
		// 本地端下載
		String outputPath = RESOURCE + "/result/employees.xlsx"; // 檔案輸出路徑
		ExcelUtil.downloadLocal(book, outputPath);

	}

}
