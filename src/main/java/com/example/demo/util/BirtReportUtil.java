package com.example.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BirtReportUtil {

	private static IReportEngine birtReportEngine;

	static {
		EngineConfig config = new EngineConfig();
		try {

			// 取得 fontsConfig.xml 字體配置檔 (須放在 src/main/resources 目錄下，否則會讀不到)
			URL url = BirtReportUtil.class.getClassLoader().getResource("fontsConfig.xml");
			config.setFontConfig(url);

			Platform.startup(config);
			IReportEngineFactory factory = (IReportEngineFactory) Platform
					.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);

			birtReportEngine = factory.createReportEngine(config);
		} catch (Exception e) {
			log.error("BIRT Engine 初始化失敗", e);
		}
	}

	/**
	 * 建立 pdf Report
	 *
	 * @param filePath    檔案路徑
	 * @param fileName    檔案名稱
	 * @param dataContext Map<key, 要被遍歷渲染的內容清單>
	 * @param parameters  參數
	 */
	public static ByteArrayResource generatePdfReport(String filePath, String fileName, Map<String, Object> parameters,
			Map<String, Object> dataContext) {
		InputStream inputStream = getResourceInputStream(filePath, fileName);
		return generatePdfReport(inputStream, parameters, dataContext);
	}

	/**
	 * 產生 PDF 格式的報表
	 *
	 * @param inputStream 報表設計檔的 InputStream (.rptdesign)
	 * @param params      報表參數 Map，key 為參數名稱，value 為參數值（可為 null）
	 * @param dataContext Map<key, 要被遍歷渲染的內容清單>
	 * @return 包含產生後 PDF 資料的 ByteArrayResource
	 * @throws EngineException BIRT 報表引擎相關異常
	 */
	public static ByteArrayResource generatePdfReport(InputStream inputStream, Map<String, Object> params,
			Map<String, Object> dataContext) {

		try {
			// 載入報表設計檔
			IReportRunnable design = birtReportEngine.openReportDesign(inputStream);

			// 建立報表執行與渲染任務
			IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design);

			// 設定報表參數（Parameter）
			if (params != null && !params.isEmpty()) {
				params.forEach(task::setParameterValue);
			}

			// 設定報表 AppContext（用於報表腳本中取得 Java 傳入的 List 等物件）
			if (dataContext != null && !dataContext.isEmpty()) {
				task.getAppContext().putAll(dataContext);
			}

			// 設定輸出格式與輸出流
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PDFRenderOption options = new PDFRenderOption();
			options.setOutputFormat("pdf");
			options.setOutputStream(bos);
			task.setRenderOption(options);

			// 執行報表渲染任務
			task.run();
			task.close();
			// 回傳報表 PDF 結果
			return new ByteArrayResource(bos.toByteArray());
		} catch (EngineException e) {
			log.error("發生錯誤，轉換失敗", e);
			return null;
		}

	}

	/**
	 * 產生 HTML 格式的報表
	 *
	 * @param inputStream 報表設計檔的 InputStream (.rptdesign)
	 * @param parameters  報表參數 Map，key 為參數名稱，value 為參數值（可為 null）
	 * @return 包含產生後 HTML 資料的 ByteArrayResource
	 * @throws EngineException BIRT 報表引擎相關異常
	 */
	public static ByteArrayResource generateHtmlReport(InputStream inputStream, Map<String, Object> parameters)
			throws EngineException {
		// 開啟報表設計檔
		IReportRunnable design = birtReportEngine.openReportDesign(inputStream);

		// 建立執行及渲染任務
		IRunAndRenderTask task = birtReportEngine.createRunAndRenderTask(design);

		// 將傳入的參數設定給報表任務
		if (parameters != null) {
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				task.setParameterValue(entry.getKey(), entry.getValue());
			}
		}
		System.setProperty("birt.viewer.fonts", "msjh.ttf");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// 建立 HTML 專用的渲染選項
		HTMLRenderOption options = new HTMLRenderOption();
		options.setOutputFormat("html");
		options.setOutputStream(bos);

		task.setRenderOption(options); // 執行報表產生與渲染
		task.run(); // 執行報表產生與渲染
		task.close(); // 關閉任務釋放資源

		return new ByteArrayResource(bos.toByteArray());
	}

	/**
	 * 從 classpath 讀取檔案
	 *
	 * @param filePath 檔案路徑
	 * @param fileName 檔案名稱
	 * @return 資料流
	 */
	public static InputStream getResourceInputStream(String filePath, String fileName) {
		String url = assemblePath(filePath, fileName);
		log.debug(url);
		// 建立類路徑資源物件
		return BirtReportUtil.class.getResourceAsStream(url);
	}

	/**
	 * 組合路徑，會自動清理多餘的斜線，忽略 null 與空字串。
	 *
	 * @param segments 路徑片段（不限數量）
	 * @return 組合後的路徑字串
	 */
	private static String assemblePath(String... segments) {
		String joined = Arrays.stream(segments).filter(Objects::nonNull)
				.map(s -> s.replaceAll("^/+", "").replaceAll("/+$", "")).filter(s -> !s.isEmpty())
				.collect(Collectors.joining("/"));
		// 確保開頭為 /
		if (!joined.startsWith("/")) {
			joined = "/" + joined;
		}
		// 確保結尾為 .rptdesign
		if (!joined.endsWith(".rptdesign")) {
			joined += ".rptdesign";
		}
		return joined;
	}

}
