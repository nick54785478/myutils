package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;
import org.jxls.util.TransformerFactory;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Jxls 工具類，透過模板 (Template) 輸出為 Excel 文件
 */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JxlsUtil {

	private static final String TEMPLATE_PATH = "jxls-template";

	/**
	 * 將 InputStream 中的資料以及模型中的資料匯出到 Excel 中，並將結果寫入給定的 OutputStream 中。
	 *
	 * @param is    匯出 Excel 範本的 InputStream
	 * @param os    匯出資料表的目標 OutputStream
	 * @param model 包含要在 Excel 中使用的資料的模型，以鍵值對的形式儲存 Map<key, List<Bean>>
	 * @throws IOException 如果在處理輸入流或輸出流時發生 I/O 錯誤
	 */
	public static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model) throws IOException {
		Context context = new Context(); // 建立一個上下文物件來保存模型中的數據

		if (model != null) {
			for (String key : model.keySet()) {
				// 將資料寫入 excel
				context.putVar(key, model.get(key));
			}
		}

		JxlsHelper jxlsHelper = JxlsHelper.getInstance();
		// 使用輸入流和輸出流建立 Transformer 對象，用於讀取 Excel 範本並將結果寫入輸出流
		Transformer transformer = jxlsHelper.createTransformer(is, os);
		jxlsHelper.processTemplate(context, transformer);
	}

	/**
	 * 將 InputStream 中的資料以及模型中的資料匯出到 Excel 中，並將結果寫入給定的 OutputStream 中 ( Multiple
	 * Sheet )。
	 * 
	 * @param is    匯出 Excel 範本的 InputStream
	 * @param os    匯出資料表的目標 OutputStream
	 * @param model 包含要在 Excel 中使用的資料的模型，以鍵值對的形式儲存 Map<SheetName, Map<key,
	 *              List<Bean>>>
	 * @throws IOException 如果在處理輸入流或輸出流時發生 I/O 錯誤
	 */
	public static void exportMultiSheetExcel(InputStream is, OutputStream os, Map<String, Map<String, Object>> model)
			throws IOException {

		Transformer transformer = TransformerFactory.createTransformer(is, os);

		AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer, true);
		List<Area> xlsAreaList = areaBuilder.build();
		xlsAreaList.stream().forEach(area -> {
			String sheetName = area.getStartCellRef().getSheetName(); // 取得 Sheet 名稱
			Context context = new Context();
			// 設定對應的變數 (如果有的話)
			if (model.containsKey(sheetName)) {
				model.get(sheetName).forEach(context::putVar);
			}
			// 動態渲染 Sheet
			area.applyAt(new CellRef(sheetName + "!A1"), context);
			area.processFormulas();
		});
		transformer.write(); // 寫入輸出
	}

	/**
	 * 將 File 轉為 FileInputStream 中的資料以及模型中的資料匯出到 Excel 中，並將結果寫入給定的 OutputStream 中。
	 * 
	 * @param inputFile  Input 文件
	 * @param outputFile Output 文件
	 * @param model      包含要在 Excel 中使用的資料的模型，以鍵值對的形式儲存 Map<key, List<Bean>>
	 */
	public static void exportExcel(File inputFile, File outputFile, Map<String, Object> model) throws IOException {
		exportExcel(new FileInputStream(inputFile), new FileOutputStream(outputFile), model);
	}

	/**
	 * 取得範本並 轉為 FileInputStream 中的資料以及模型中的資料匯出到 Excel 中，並將結果寫入給定的 OutputStream 中。
	 * 
	 * @param templateName 範本名稱
	 * @param os           OutputStream
	 * @param model        包含要在 Excel 中使用的資料的模型，以鍵值對的形式儲存 Map<key, List<Bean>>
	 */
	public static void exportExcel(String templateName, OutputStream os, Map<String, Object> model) throws IOException {
		File template = getTemplate(templateName);
		if (template != null) {
			exportExcel(new FileInputStream(template), os, model);
		}
	}

	/**
	 * 取得 jxls 模板範本
	 * 
	 * @param name 模板名稱
	 * @return File
	 */
	public static File getTemplate(String name) {
		String templatePath = JxlsUtil.class.getClass().getClassLoader().getResource(TEMPLATE_PATH).getPath();
		File template = new File(templatePath, name);
		if (template.exists()) {
			return template;
		}
		return null;
	}

}