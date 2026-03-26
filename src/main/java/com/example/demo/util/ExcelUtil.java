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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelUtil {

	// ### 對外 API ###

	/**
	 * 讀取 Excel 並解析所有工作表。
	 *
	 * <p>
	 * 核心流程： 1. 打開 Excel InputStream 建立 Workbook<br>
	 * 2. 遍歷每個 Sheet，呼叫 {@link #parseSheet(Sheet)} 解析內容<br>
	 * 3. 返回 Map，key 為 Sheet 名稱，value 為 List<Map<String, String>> 表示資料列
	 * </p>
	 *
	 * @param inputStream Excel InputStream
	 * @return Map<SheetName, List<RowData>>
	 * @throws IOException 當讀取 Excel 發生錯誤時
	 */
	public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream) throws IOException {

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				String sheetName = sheet.getSheetName();
				result.put(sheetName, parseSheet(sheet));
			}
		}

		return result;
	}

	/**
	 * 讀取指定 Sheet 的 Excel 資料。
	 *
	 * @param inputStream Excel InputStream
	 * @param sheetName   指定的 Sheet 名稱
	 * @return List<RowData>，若 Sheet 不存在則返回空 List
	 * @throws IOException 當讀取 Excel 發生錯誤時
	 */
	public static List<Map<String, String>> readExcelData(InputStream inputStream, String sheetName)
			throws IOException {

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {
			Sheet sheet = workbook.getSheet(sheetName);
			return sheet == null ? List.of() : parseSheet(sheet);
		}
	}

	/**
	 * 讀取指定多個 Sheet 的 Excel 資料。
	 *
	 * @param inputStream   Excel InputStream
	 * @param sheetNameList 需要讀取的 Sheet 名稱列表
	 * @return Map<SheetName, List<RowData>>
	 * @throws IOException 當讀取 Excel 發生錯誤時
	 */
	public static Map<String, List<Map<String, String>>> readExcelData(InputStream inputStream,
			List<String> sheetNameList) throws IOException {

		Map<String, List<Map<String, String>>> result = new HashMap<>();

		try (Workbook workbook = new XSSFWorkbook(inputStream)) {

			for (String sheetName : sheetNameList) {
				Sheet sheet = workbook.getSheet(sheetName);
				result.put(sheetName, sheet == null ? List.of() : parseSheet(sheet));
			}
		}

		return result;
	}

	/**
	 * 解析單一 Sheet。
	 *
	 * <p>
	 * 流程： 1. 讀取第一列作為 Header<br>
	 * 2. 從第二列開始讀取資料<br>
	 * 3. 將每列資料轉成 Map<Header, Value><br>
	 * 4. 空列過濾
	 * </p>
	 *
	 * @param sheet Sheet 對象
	 * @return List<RowData> 每列資料為 Map<String,String>
	 */
	private static List<Map<String, String>> parseSheet(Sheet sheet) {

		List<Map<String, String>> data = new ArrayList<>();
		if (sheet == null) {
			return data;
		}

		Row headerRow = sheet.getRow(0);
		if (headerRow == null) {
			return data;
		}

		// 處理 Header 資料
		List<String> headers = new ArrayList<>();
		for (Cell cell : headerRow) {
			headers.add(StringUtils.trim(parseCellValue(cell)));
		}

		// 處理 Rows 資料
		for (int r = 1; r <= sheet.getLastRowNum(); r++) {

			Row row = sheet.getRow(r);
			if (row == null) {
				continue;
			}

			Map<String, String> rowData = new LinkedHashMap<>();
			for (int c = 0; c < headers.size(); c++) {
				Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String value = StringUtils.trim(parseCellValue(cell));
				rowData.put(headers.get(c), value);
			}

			// 過濾空列
			if (isRowEmpty(rowData)) {
				continue;
			}

			data.add(rowData);
		}

		return data;
	}

	/**
	 * 判斷一列是否為空（核心防禦）。
	 *
	 * @param rowData Map<Header, Value>
	 * @return true 若所有值皆為空白
	 */
	private static boolean isRowEmpty(Map<String, String> rowData) {
		return rowData.values().stream().allMatch(StringUtils::isBlank);
	}

	/**
	 * 解析 Cell 值。
	 *
	 * <p>
	 * 支援型態：
	 * <ul>
	 * <li>STRING</li>
	 * <li>NUMERIC</li>
	 * <li>BOOLEAN</li>
	 * <li>FORMULA (優先解析數值，失敗則解析字串)</li>
	 * </ul>
	 * </p>
	 *
	 * @param cell Excel Cell
	 * @return 字串型態的 Cell 值，空 Cell 回傳 ""
	 */
	private static String parseCellValue(Cell cell) {

		if (cell == null) {
			return "";
		}

		return switch (cell.getCellType()) {
		case STRING -> cell.getStringCellValue();
		case NUMERIC -> String.valueOf(cell.getNumericCellValue());
		case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
		case FORMULA -> {
			try {
				yield String.valueOf(cell.getNumericCellValue());
			} catch (Exception e) {
				yield cell.getStringCellValue();
			}
		}
		default -> "";
		};
	}

	// ### Export Excel ###

	/**
	 * 將資料導出為 InputStreamResource，方便 Spring ResponseEntity 使用。
	 *
	 * @param sheetName  Sheet 名稱
	 * @param headerList 表頭 List
	 * @param rowDataSet 資料列集合 (物件型態)
	 * @return InputStreamResource
	 */
	public static InputStreamResource exportDataAsResource(String sheetName, List<String> headerList,
			List<?> rowDataSet) {

		XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			book.write(bos);
			byte[] bookByteArray = bos.toByteArray();
			book.close();
			return new InputStreamResource(new ByteArrayInputStream(bookByteArray));
		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗 ", e);
			return null;
		}
	}

	/**
	 * 將資料導出為 byte[]。
	 *
	 * @param sheetName  Sheet 名稱
	 * @param headerList 表頭 List
	 * @param rowDataSet 資料列集合
	 * @return byte[]
	 */
	public static byte[] exportDataAsByteArray(String sheetName, List<String> headerList, List<?> rowDataSet) {

		XSSFWorkbook book = processWorkbook(sheetName, headerList, rowDataSet);

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			book.write(bos);
			book.close();
			return bos.toByteArray();
		} catch (IOException e) {
			log.error("轉換錯誤，產生報表失敗", e);
			return new byte[0];
		}
	}

	/**
	 * 建立 Workbook。
	 *
	 * @param sheetName  Sheet 名稱
	 * @param headerList 表頭
	 * @param rowDataSet 物件型態資料列
	 * @return XSSFWorkbook
	 */
	public static XSSFWorkbook processWorkbook(String sheetName, List<String> headerList, List<?> rowDataSet) {

		XSSFWorkbook book = new XSSFWorkbook();
		XSSFSheet sheet = book.createSheet(sheetName);

		Object[] headers = headerList.toArray();
		List<Object[]> dataset = new ArrayList<>();
		rowDataSet.forEach(e -> dataset.add(convertObjectToArray(e)));

		importData(sheet, headers, dataset);

		return book;
	}

	/**
	 * 匯入資料到 Sheet。
	 *
	 * @param sheet      Sheet
	 * @param header     表頭 Object[]
	 * @param rowDataSet List<Object[]> 資料列
	 */
	public static void importData(XSSFSheet sheet, Object[] header, List<Object[]> rowDataSet) {

		int rowIdx = -1;

		if (header != null) {
			rowDataSet.add(0, header); // header 放到第一列
		} else {
			rowIdx = 0;
		}

		for (Object[] arrs : rowDataSet) {

			XSSFRow row = sheet.createRow(++rowIdx);

			int colIdx = -1;
			for (Object field : arrs) {

				XSSFCell cell = row.createCell(++colIdx);

				switch (field.getClass().getSimpleName()) {
				case "String":
					cell.setCellValue((String) field);
					break;
				case "Integer":
					cell.setCellValue((Integer) field);
					break;
				case "Long":
					cell.setCellValue((Long) field);
					break;
				case "Double":
					cell.setCellValue((Double) field);
					break;
				case "Date":
					cell.setCellValue(DateFormatUtils.format((Date) field, "yyyy/MM/dd"));
					break;
				case "BigDecimal":
					cell.setCellValue(((BigDecimal) field).doubleValue());
					break;
				default:
					cell.setCellValue("");
				}

//				switch (field) {
//				case String str -> cell.setCellValue(str);
//				case Integer i -> cell.setCellValue(i);
//				case Long l -> cell.setCellValue(l);
//				case Double d -> cell.setCellValue(d);
//				case Date date -> cell.setCellValue(DateFormatUtils.format(date, "yyyy/MM/dd"));
//				case BigDecimal bd -> cell.setCellValue(bd.doubleValue());
//				default -> cell.setCellValue("");
//				}
			}
		}
	}

	/**
	 * 將物件轉換為 Object[]，方便匯出 Excel。
	 *
	 * <p>
	 * 使用 Reflection 讀取物件所有欄位，並支援 private 欄位。
	 * </p>
	 *
	 * @param obj 任意物件
	 * @return Object[] 欄位值陣列
	 */
	private static Object[] convertObjectToArray(Object obj) {

		Field[] fields = obj.getClass().getDeclaredFields();
		Object[] objectArray = new Object[fields.length];

		try {
			for (int i = 0; i < fields.length; i++) {
				ReflectionUtils.makeAccessible(fields[i]);
				objectArray[i] = fields[i].get(obj);
			}
		} catch (IllegalAccessException e) {
			log.error("物件轉換發生非預期的錯誤");
		}

		return objectArray;
	}
}