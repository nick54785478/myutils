package com.example.demo.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
		CONFIG = new Configuration(new Version("2.3.31")); // FreeMarker 版本
		CONFIG.setDefaultEncoding("UTF-8"); // 設定 模板檔案讀取時的字元編碼
	}

	/**
	 * 從 InputStream 生成 FreeMarker 模板並渲染 HTML
	 *
	 * @param templateName 模板名稱（僅用作識別）
	 * @param inputStream  模板 InputStream
	 * @param model        模板資料 Map
	 * @return 渲染後 HTML
	 */
	public static String renderTemplate(String templateName, InputStream inputStream, Map<String, Object> model) {
		try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
			Template template = new Template(templateName, reader, CONFIG);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (IOException e) {
			throw new RuntimeException("Failed to read template from InputStream", e);
		} catch (TemplateException e) {
			throw new RuntimeException("Error processing template: " + templateName, e);
		}
	}

	/**
	 * 從 InputStream 生成 FreeMarker 模板並渲染 HTML（無 model）
	 *
	 * @param templateName 模板名稱（僅用作識別）
	 * @param inputStream  模板 InputStream
	 * @return 渲染後 HTML
	 */
	public static String renderTemplate(String templateName, InputStream inputStream) {
		// 傳空 map 給原方法
		return renderTemplate(templateName, inputStream, Map.of());
	}

	/**
	 * 將 內容放進空白範本內渲染
	 *
	 * @param templateName    模板名稱（僅用作識別與 log）
	 * @param templateContent 模板內容（HTML + FreeMarker 語法）
	 * @param model           模板資料 Map
	 * @return 渲染後 HTML
	 */
	public static String renderTemplateFromString(String templateName, String templateContent,
			Map<String, Object> model) {
		if (templateContent == null || templateContent.isBlank()) {
			return null;
		}

		try (StringReader reader = new StringReader(templateContent)) {
			Template template = new Template(templateName, reader, CONFIG);
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (IOException e) {
			log.error("Failed to read template content, templateName={}", templateName, e);
			return null;
		} catch (TemplateException e) {
			log.error("Error processing template content, templateName={}", templateName, e);
			return null;
		}
	}

	/**
	 * 從模板字串生成 FreeMarker 模板並渲染 HTML（無 model）
	 *
	 * @param templateName    模板名稱（僅用作識別與 log）
	 * @param templateContent 模板內容
	 * @return 渲染後 HTML
	 */
	public static String renderTemplateFromString(String templateName, String templateContent) {
		return renderTemplateFromString(templateName, templateContent, Map.of());
	}
}
