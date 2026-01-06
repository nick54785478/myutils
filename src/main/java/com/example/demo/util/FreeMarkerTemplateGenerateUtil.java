package com.example.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FreeMarkerTemplateGenerateUtil {

	private static final Configuration CONFIG;

	static {
		// FreeMarker 版本
		CONFIG = new Configuration(new Version("2.3.31"));
		// templates 放在 resources/templates
		CONFIG.setClassForTemplateLoading(FreeMarkerTemplateGenerateUtil.class, "/freemarker");
		// 設定 模板檔案讀取時的字元編碼
		CONFIG.setDefaultEncoding("UTF-8");
	}

	/**
	 * 從 InputStream 生成 FreeMarker 模板並渲染 HTML
	 *
	 * @param inputStream  模板 InputStream
	 * @param templateName 模板名稱（僅用作識別）
	 * @param model        模板資料 Map
	 * @return 渲染後 HTML
	 */
	public static String processTemplate(InputStream inputStream, String templateName, Map<String, Object> model) {
		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			Template template = new Template(templateName, reader, CONFIG);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (IOException e) {
			log.error("Failed to read template from InputStream", e);
			return null;
		} catch (TemplateException e) {
			log.error("Error processing template: " + templateName, e);
			return null;
		}
	}

	/**
	 * 從 InputStream 生成 FreeMarker 模板並渲染 HTML（無 model）
	 *
	 * @param templateName 模板名稱（僅用作識別）
	 * @param inputStream  模板 InputStream
	 * @return 渲染後 HTML
	 */
	public static String processTemplate(InputStream inputStream, String templateName) {
		// 傳空 map 給原方法
		return processTemplate(inputStream, templateName, Map.of());
	}
}
