package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import com.example.demo.model.employee.Employee;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
		ExcelUtilTest.book = ExcelUtil.processWorkbook("employee", this.headers, this.employees);
	}

	/**
	 * 讀取 Excel 檔
	 * 
	 * @throws IOException
	 */
	@Test
	void testReadExcelData() throws IOException {
		// 寫入並建立 Byte[]
		byte[] byteArray = ExcelUtil.exportDataAsByteArray("employee", headers, employees);
		List<Map<String, String>> excelData = ExcelUtil.readExcelData(new ByteArrayInputStream(byteArray), "employee");
		System.out.println("excelData: " + excelData);
	}

	@Test
	void testReadExcelDataWithoutSheetNameList() throws IllegalStateException, IOException {
		InputStreamResource resource = ExcelUtil.exportDataAsResource("employee", headers, employees);
		Map<String, List<Map<String, String>>> excelData = ExcelUtil.readExcelData(resource.getInputStream());
		System.out.println(excelData);
	}

	/**
	 * 本地端進行下載
	 */
	@AfterAll
	static void tearDown() throws Exception {
		// 本地端下載
		String outputPath = RESOURCE + "/excel/result/employees.xlsx"; // 檔案輸出路徑
		downloadLocal(book, outputPath);

	}

	/**
	 * 本地端下載
	 * 
	 * @param book
	 * @param path 檔案下載路徑
	 */
	private static void downloadLocal(XSSFWorkbook book, String path) {
		try (FileOutputStream os = new FileOutputStream(path)) {
			book.write(os);
		} catch (FileNotFoundException e) {
			log.error("File Not Found ", e);
		} catch (IOException e) {
			log.error("轉換錯誤");
		}
	}
}
