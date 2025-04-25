package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.util.JxlsHelper;

public class JxlsUtil {

	private static final String TEMPLATE_PATH = "jxls-template";

	/**
	 * 將 InputStream 中的資料以及模型中的資料匯出到Excel中，並將結果寫入給定的 OutputStream 中。
	 *
	 * @param 是要匯出Excel範本的InputStream
	 * @param os 輸出導出資料的目標 OutputStream
	 * @param model 包含要在Excel中使用的資料的模型，以鍵值對的形式儲存
	 * @throws IOException 如果在處理輸入流或輸出流時發生 I/O 錯誤
	 */
	public static void exportExcel(InputStream is, OutputStream os, Map<String, Object> model) throws IOException {
		Context context = new Context();  // 建立一個上下文物件來保存模型中的數據 
		if (model != null) {
			for (String key : model.keySet()) {
				// 將資料寫入 excel
				context.putVar(key, model.get(key));
			}
		}
		
		JxlsHelper jxlsHelper = JxlsHelper.getInstance();
		
		// 使用輸入流和輸出流建立 Transformer 對象，用於讀取 Excel 範本並將結果寫入輸出流
		Transformer transformer = jxlsHelper.createTransformer(is, os);

		// 取得JexlExpressionEvaluator，用於簡化表達式
//		JexlExpressionEvaluator evaluator = (JexlExpressionEvaluator) transformer.getTransformationConfig()
//				.getExpressionEvaluator();
//		Map<String, Object> funcs = new HashMap<String, Object>(); // 將自訂函數新增至函數映射中
		// funcs.put("utils", new JxlsUtils()); //添加自定义功能
		// evaluator.getJexlEngine().setFunctions(funcs); // 設定Jexl引擎的函數

		jxlsHelper.processTemplate(context, transformer);
	}

	public static void exportExcel(File xlsx, File out, Map<String, Object> model)
			throws FileNotFoundException, IOException {
		exportExcel(new FileInputStream(xlsx), new FileOutputStream(out), model);
	}

	public static void exportExcel(String templateName, OutputStream os, Map<String, Object> model)
			throws FileNotFoundException, IOException {
		File template = getTemplate(templateName);
		if (template != null) {
			exportExcel(new FileInputStream(template), os, model);
		}
	}

	// 取得 jxls 模板template
	public static File getTemplate(String name) {
		String templatePath = JxlsUtil.class.getClass().getClassLoader().getResource(TEMPLATE_PATH).getPath();
		File template = new File(templatePath, name);
		if (template.exists()) {
			return template;
		}
		return null;
	}

	// 日期格式化
	public String dateFmt(Date date, String fmt) {
		if (date == null) {
			return "";
		}
		try {
			SimpleDateFormat dateFmt = new SimpleDateFormat(fmt);
			return dateFmt.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	// 轉換數字
	public String parseInt(String integer) {
		if (Objects.isNull(integer)) {
			return "";
		} else {
			return "" + integer;
		}
	}

	// if 判斷
	public Object ifelse(boolean b, Object o1, Object o2) {
		return b ? o1 : o2;
	}
}
