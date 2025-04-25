package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import com.example.demo.model.employee.Employee;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

	/**
	 * 參考
	 */
	public void exportExcel(List<String> headers, List<Employee> content) {

		try {
			// 宣告XSSFWorkbook
			Workbook wb = new XSSFWorkbook();
			// 建立分頁
			Sheet sheet = wb.createSheet("工作表");
			Row row = null; // 宣告列物件
			Cell cell = null; // 宣告表格物件

			// 宣告標題列 物件
			row = sheet.createRow(0); // 第一列

			// 標題處理
			for (int i = 0; i < headers.size(); i++) {
				// 宣告表格物件
				cell = row.createCell(i); // 第i行
				cell.setCellValue(headers.get(i));
			}

			// 內容處理
			for (int i = 0; i < content.size(); i++) {
				row = sheet.createRow(i + 1); // 第i+1列
				cell = row.createCell(0);
				cell.setCellValue(content.get(i).getName());
				cell = row.createCell(1);
				cell.setCellValue(content.get(i).getId());
				cell = row.createCell(2);
				cell.setCellValue(content.get(i).getEngName());
				cell = row.createCell(3);
				cell.setCellValue(content.get(i).getAge());
				cell = row.createCell(4);
				cell.setCellValue(content.get(i).getEmail());
			}

			// 設定檔案輸出串流到指定位置
			FileOutputStream fileOut = new FileOutputStream("target/generated-sources/employees.xlsx");
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Apache POI 匯入資料至工作表
	 * 
	 * @param sheet
	 * @param headerList 標題列資料
	 * @param dataList    資料內容
	 * @return InputStreamResource
	 */
	public static InputStreamResource exportDataAsResource(List<String> headerList, List<Object> dataList) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(headerList, dataList);

		// 建立 Resource 往前端送
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			book.write(bos);
			byte[] bookByteArray = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(bookByteArray);
			book.close();
			return new InputStreamResource(bis);

		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗", e);
			return null;
		}
	}

	/**
	 * 建立 Excel 表單資料
	 * @param headerList 標題列資料
	 * @param dataList    資料內容
	 */
	public static XSSFWorkbook processWorkbook(List<String> headerList, List<? extends Object> dataList) {
		// 新建工作簿
		XSSFWorkbook book = new XSSFWorkbook();
		// 建立工作表
		XSSFSheet sheet = book.createSheet("Books");

		Object[] headers = headerList.toArray();

		// 資料轉換
		List<Object[]> dataset = new ArrayList<>();
		dataList.stream().forEach(e -> {
			dataset.add(convertObjectToArray(e));
		});

		// 匯入資料至工作表
		importData(sheet, headers, dataset);

		return book;
	}

	/**
	 * Apache POI 匯入資料至工作表
	 * 
	 * @param sheet
	 * @param headerList 標題列資料
	 * @param dataset    資料內容
	 * @return InputStreamResource
	 */
	public static byte[] exportDataAsByteArray(List<String> headerList, List<? extends Object> dataList) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(headerList, dataList);

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			book.write(bos);
			book.close();
			return bos.toByteArray();

		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗", e);
			return null;
		}
	}

	/**
	 * Apache POI 匯入資料至工作表
	 * 
	 * @param sheet
	 * @param dataset
	 * @return
	 */
	public static void importData(XSSFSheet sheet, Object[] header, List<Object[]> dataset) {
		int rowIdx = -1;

		if (!Objects.isNull(header)) {
			// 新增第一筆
			dataset.add(0, header);
		} else {
			rowIdx = 0;
		}

		for (Object[] arrs : dataset) {
			// 建立列
			XSSFRow row = sheet.createRow(++rowIdx);

			int colIdx = -1;
			for (Object field : arrs) {
				// 建立單元格
				XSSFCell cell = row.createCell(++colIdx);

				// 單元格寫入內容
				if (field instanceof String) {
					cell.setCellValue((String) field);
				} else if (field instanceof Integer) {
					cell.setCellValue((Integer) field);
				} else if (field instanceof Long) {
					cell.setCellValue((Long) field);
				} else if (field instanceof Double) {
					cell.setCellValue((Double) field);
				} else if (field instanceof Date) {
					String d = DateFormatUtils.format((Date) field, "yyyy/MM/dd");
					cell.setCellValue(d);
				} else if (field instanceof BigDecimal) {
					BigDecimal bd = (BigDecimal) field;
					double d = bd.doubleValue();
					cell.setCellValue(d);
				} else {
					cell.setCellValue("");
				}
			}
		}
	}

	/**
	 * 讀取Excel 單一sheet資料
	 * 
	 * @param inputStream : 資料流
	 * @param sheetName   : 工作表名稱
	 * @return List<map<String, String>>: List<map<標題, 值>>
	 * @throws IOException
	 */
	public static List<Map<String, String>> readExcelData(InputStream inputStream, String sheetName)
			throws IOException {

		List<Map<String, String>> result = new ArrayList<>();

		Workbook workbook = new XSSFWorkbook(inputStream);

		processWorkbook(workbook, sheetName, result);

		workbook.close(); // 關閉工作簿以釋放資源
		return result;
	}

	/**
	 * 處理 多個sheet
	 * 
	 * @param Workbook  workbook
	 * @param sheetName 表名
	 * @param 處理後的資料
	 */
	private static void processWorkbook(Workbook workbook, String sheetName, List<Map<String, String>> result) {

		Sheet sheet = workbook.getSheet(sheetName);

		// 獲取第一列（標題列）
		Row titleRow = sheet.getRow(0);

		// 迭代列 (從第 2 列開始)
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Map<String, String> map = new HashMap<>();
			// 資料列
			Row row = sheet.getRow(rowIndex);

			if (row == null) {
				break;
			}

			// 遍歷標題行的每一個單元格
			Iterator<Cell> titleCellIterator = titleRow.cellIterator();
			int cellIndex = 0;
			while (titleCellIterator.hasNext()) {
				String key = StringUtils.trim(parseCellValue(titleCellIterator.next()));
				String value = parseCellValue(row.getCell(cellIndex));
				if (StringUtils.isNotBlank(key)) {
					map.put(key, value);
				}

				cellIndex++;

			}
			log.info("map: {}", map);
			result.add(map);
		}

	}

	/**
	 * 多張表資料
	 * 
	 * @throws IOException
	 */
	public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream,
			List<String> sheetNameList) throws IOException {

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		Workbook workbook = new XSSFWorkbook(inputStream);

		sheetNameList.stream().forEach(sheetName -> {
			List<Map<String, String>> list = new ArrayList<>();
			processWorkbook(workbook, sheetName, list);
			result.put(sheetName, list);
		});

		return result;

	}

	/**
	 * 轉換單元格內的值
	 */
	private static String parseCellValue(Cell cell) {
		String cellValue = "";
		switch (cell.getCellType()) {
		case STRING:
			cellValue = cell.getStringCellValue();
			break;
		case NUMERIC:
			cellValue = String.valueOf(cell.getNumericCellValue());
			break;
		case BOOLEAN:
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case BLANK:
			break;
		default:
			break;
		}

		return cellValue;
	}

	/**
	 * 動態轉換物件為 Object[]
	 * 
	 * @param Class
	 * @return Object[]
	 */
	private static Object[] convertObjectToArray(Object obj) {
		Class<?> clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Object[] objectArray = new Object[fields.length];

		try {
			for (int i = 0; i < fields.length; i++) {
				fields[i].setAccessible(true);
				objectArray[i] = fields[i].get(obj);
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		return objectArray;
	}

	/**
	 * 本地端下載
	 * 
	 * @param book
	 * @param path 檔案下載路徑
	 */
	public static void downloadLocal(XSSFWorkbook book, String path) {
		try (FileOutputStream os = new FileOutputStream(path)) {
			book.write(os);
		} catch (FileNotFoundException e) {
			log.error("File Not Found ", e);
		} catch (IOException e) {
			log.error("轉換錯誤");
		}
	}

}
