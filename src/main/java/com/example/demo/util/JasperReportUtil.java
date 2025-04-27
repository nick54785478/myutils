package com.example.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleDocxReportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleTextExporterConfiguration;
import net.sf.jasperreports.export.SimpleTextReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JasperReportUtil {

	/**
	 * 建立報表並下載
	 * 
	 * @param type           下載檔案種類
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	public static ResponseEntity<ByteArrayResource> generateReportToDownload(ReportType type, String jasperFilename,
			String outputFileName, Collection<?> list, Map<String, Object> parameters) throws SQLException {
		ByteArrayResource resource = generateReport(type, jasperFilename, list, parameters);

		if (resource != null) {
			String encodedFilename = outputFileName;
			try {
				encodedFilename = URLEncoder.encode(outputFileName, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.error("Output filename encoding failed!");
			}

			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
			headers.add(HttpHeaders.PRAGMA, "no-cache");
			headers.add(HttpHeaders.EXPIRES, "0");
			headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + encodedFilename);
			headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
			headers.add(HttpHeaders.CONTENT_ENCODING, "UTF-8");
			headers.setContentLength(resource.contentLength());
			headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			return new ResponseEntity<>(resource, headers, HttpStatus.OK);
		} else {
			log.debug("generateReportToDownload() resource is null.");
			return new ResponseEntity<>(resource, HttpStatus.NO_CONTENT);
		}
	}

	/**
	 * 根據文件種類建立報表
	 * 
	 * @param type           下載檔案種類
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	private static ByteArrayResource generateReport(ReportType type, String jasperFilename, Collection<?> list,
			Map<String, Object> parameters) throws SQLException {
		switch (type) {
		case PDF:
			return generateReportToPDF(jasperFilename, list, parameters);
		case XLSX:
			return generateReportToXLSX(jasperFilename, list, parameters);
		case DOCX:
			return generateReportToDOCX(jasperFilename, list, parameters);
		case TXT:
			return generateReportToTXT(jasperFilename, list, parameters);

		default:
			return null;
		}
	}

	/**
	 * 建立pdf報表
	 * 
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	private static ByteArrayResource generateReportToPDF(String jasperFilename, Collection<?> list,
			Map<String, Object> parameters) throws SQLException {

		try (var outputStream = new ByteArrayOutputStream();) {

			JasperPrint jasperPrint = fillReport(jasperFilename, list, parameters);

			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);

			byte[] reportContent = outputStream.toByteArray();
			return new ByteArrayResource(reportContent);
		} catch (IOException | JRException e) {
			log.error("generate report to PDF error: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 建立xlsx報表
	 * 
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	private static ByteArrayResource generateReportToXLSX(String jasperFilename, Collection<?> list,
			Map<String, Object> parameters) {

		try (var outputStream = new ByteArrayOutputStream();) {

			JasperPrint jasperPrint = fillReport(jasperFilename, list, parameters);

			JRXlsxExporter exporter = new JRXlsxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
			configuration.setSheetNames(new String[] { "sheet1" });
			configuration.setRemoveEmptySpaceBetweenRows(Boolean.TRUE);
			configuration.setRemoveEmptySpaceBetweenColumns(Boolean.FALSE);
			configuration.setOnePagePerSheet(Boolean.FALSE);
			configuration.setFontSizeFixEnabled(Boolean.TRUE);
			exporter.setConfiguration(configuration);
			exporter.exportReport();

			byte[] reportContent = outputStream.toByteArray();
			return new ByteArrayResource(reportContent);
		} catch (IOException | JRException e) {
			log.error("generate report to XLSX error: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 建立 doc 報表
	 * 
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	private static ByteArrayResource generateReportToDOCX(String jasperFilename, Collection<?> list,
			Map<String, Object> parameters) {

		try (var outputStream = new ByteArrayOutputStream();) {

			JasperPrint jasperPrint = fillReport(jasperFilename, list, parameters);

			JRDocxExporter exporter = new JRDocxExporter();
			exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
			SimpleDocxReportConfiguration configuration = new SimpleDocxReportConfiguration();
			configuration.setFlexibleRowHeight(true);
			exporter.setConfiguration(configuration);
			exporter.exportReport();

			byte[] reportContent = outputStream.toByteArray();
			return new ByteArrayResource(reportContent);
		} catch (IOException | JRException e) {
			log.error("generate report to PDF error: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 建立 txt 檔
	 * 
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	private static ByteArrayResource generateReportToTXT(String jasperFilename, Collection<?> list,
			Map<String, Object> parameters) throws SQLException {

		try (var outputStream = new ByteArrayOutputStream();) {
			JasperPrint jasperPrint = fillReport(jasperFilename, list, parameters);

			JRTextExporter textExporter = new JRTextExporter();
			SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);

			SimpleTextExporterConfiguration textExporterConfiguration = new SimpleTextExporterConfiguration();
			textExporterConfiguration.setOverrideHints(false);

			SimpleTextReportConfiguration textReportConfiguration = new SimpleTextReportConfiguration();
			textReportConfiguration.setCharHeight(Float.valueOf(20));
			textReportConfiguration.setCharWidth(5.6f);
			textReportConfiguration.setOverrideHints(false);

			textExporter.setExporterInput(exporterInput);
			textExporter.setConfiguration(textExporterConfiguration);
			textExporter.setConfiguration(textReportConfiguration);
			textExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

			textExporter.exportReport();

			byte[] reportContent = outputStream.toByteArray();
			return new ByteArrayResource(reportContent);
		} catch (IOException | JRException e) {
			log.error("generate report to TXT error: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 建立 txt 文字
	 * 
	 * @param jasperFileName
	 * @param outputFileName
	 * @param list           用於 Detail Band 的 物件集合
	 * @param parameters     設置於 Jasper Report 中的參數
	 */
	public static String exportToTxt(String jasperFilename, Collection<?> list, Map<String, Object> parameters)
			throws JRException, IOException {
		try (var outputStream = new ByteArrayOutputStream();) {
			JasperPrint jasperPrint = fillReport(jasperFilename, list, parameters);

			JRTextExporter textExporter = new JRTextExporter();
			SimpleExporterInput exporterInput = new SimpleExporterInput(jasperPrint);

			SimpleTextExporterConfiguration textExporterConfiguration = new SimpleTextExporterConfiguration();
			textExporterConfiguration.setOverrideHints(false);

			SimpleTextReportConfiguration textReportConfiguration = new SimpleTextReportConfiguration();
			textReportConfiguration.setCharHeight(Float.valueOf(20));
			textReportConfiguration.setCharWidth(5.6f);
			textReportConfiguration.setOverrideHints(false);

			textExporter.setExporterInput(exporterInput);
			textExporter.setConfiguration(textExporterConfiguration);
			textExporter.setConfiguration(textReportConfiguration);
			textExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

			textExporter.exportReport();

			byte[] reportContent = outputStream.toByteArray();
			return new String(reportContent, StandardCharsets.UTF_8);
		} catch (IOException | JRException e) {
			log.error("generate report to TXT error: {}", e.getMessage());
			throw e;
		}
	}

	/**
	 * 套版
	 * 
	 * @param jasperFilename
	 * @param list
	 * @param parameters
	 * @return
	 * @throws JRException
	 * @throws IOException
	 */
	private static JasperPrint fillReport(String jasperFilename, Collection<?> list, Map<String, Object> parameters)
			throws JRException, IOException {
		JRDataSource datasource = new JRBeanCollectionDataSource(list);

		// 取得 Jasper 檔 (Azure k8s 環境可能會抓不到，需存到 如: Azure Blob 等位置)
		var jasper = new ClassPathResource("report" + File.separator + jasperFilename + ".jasper");
		return JasperFillManager.fillReport(jasper.getInputStream(), parameters, datasource);
	}

	public enum ReportType {
		PDF, TXT, XLSX, DOCX
	}
}
