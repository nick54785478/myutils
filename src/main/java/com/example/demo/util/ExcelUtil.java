package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 工具類
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

	/**
	 * 匯入資料至工作表
	 * 
	 * @param sheetName  被建立的 Sheet Name
	 * @param headerList 標題列資料
	 * @param rowDataSet 資料內容 (列資料)
	 * @return InputStreamResource 資料流
	 */
	public static InputStreamResource exportDataAsResource(String sheetName, List<String> headerList,
			List<? extends Object> rowDataSet) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);

		// 建立 Resource 往前端送
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
			book.write(bos);
			byte[] bookByteArray = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(bookByteArray);
			book.close();
			return new InputStreamResource(bis);
		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗 ", e);
			return null;
		}
	}

	/**
	 * 匯出資料為資料流 byte[]
	 * 
	 * @param sheetName  被建立的 Sheet Name
	 * @param headerList 標題列資料
	 * @param rowDataSet 資料內容 (列資料)
	 * @return InputStreamResource
	 */
	public static byte[] exportDataAsByteArray(String sheetName, List<String> headerList,
			List<? extends Object> rowDataSet) {
		// 處理標題及內容資料
		XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);
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
	 * 建立 Excel 表單資料
	 * 
	 * @param sheetName  被建立的 Sheet Name
	 * @param headerList 標題列資料
	 * @param rowDataSet 資料內容 (列資料)
	 * @return XSSFWorkbook 表單資料
	 */
	public static XSSFWorkbook processWorkbook(String sheetName, List<String> headerList,
			List<? extends Object> rowDataSet) {
		// 新建工作簿
		XSSFWorkbook book = new XSSFWorkbook();
		// 建立工作表
		XSSFSheet sheet = book.createSheet(sheetName);
		// 轉換 Header List
		Object[] headers = headerList.toArray();
		// 資料轉換
		List<Object[]> dataset = new ArrayList<>();
		rowDataSet.stream().forEach(e -> {
			dataset.add(convertObjectToArray(e));
		});
		// 匯入資料至工作表
		importData(sheet, headers, dataset);
		return book;
	}

	/**
	 * 匯入資料
	 * 
	 * @param sheet      表單資料
	 * @param header     標題資料
	 * @param rowDataSet 單元格資料列表 (列資料)
	 */
	public static void importData(XSSFSheet sheet, Object[] header, List<Object[]> rowDataSet) {
		int rowIdx = -1;

		if (!Objects.isNull(header)) {
			// 新增第一筆
			rowDataSet.add(0, header);
		} else {
			rowIdx = 0;
		}

		for (Object[] arrs : rowDataSet) {
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
	 * 讀取 Excel 單一 sheet 資料
	 * 
	 * @param inputStream 資料流
	 * @param sheetName   工作表名稱
	 * @return List<map<String, String>>: List<map<標題, 值>> 一個 Map 是一列資料
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
	 * 讀取多張表資料，並輸出 Map<sheetName, List<Header, Cell>>
	 * 
	 * @param inputStream 資料流
	 * @return Map<SheetName, List<Map<Header, Cell>>>
	 * @throws IOException
	 */
	public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream,
			List<String> sheetNameList) throws IOException {
		Map<String, List<Map<String, String>>> result = new HashMap<>();
		// 將 InputStream 轉為 Workbook 資料
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
	 * @param obj
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
	 * 處理 多個 sheet 並將其加入輸出資料集
	 * 
	 * @param workbook  Workbook
	 * @param sheetName 表名
	 * @param dataSet   List<Map<標題, 內容>>
	 */
	private static void processWorkbook(Workbook workbook, String sheetName, List<Map<String, String>> dataSet) {
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
			// 遍歷標題列的每一個單元格
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
			dataSet.add(map);
		}
	}

}
