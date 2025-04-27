package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PDF 工具類
 * */
@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PdfBoxUtil {

	/**
	 * 讀取 pdf 文字檔
	 * 
	 * @param file: pdf文檔
	 * @throws IOException
	 */
	public static String readPdfContent(MultipartFile file) throws IOException {
		return readPdfContent(file.getInputStream());
	}

	/**
	 * 讀取 pdf 文字檔 (註. 格式不會帶入，僅讀出文字)
	 * 
	 * @param InputStream
	 */
	public static String readPdfContent(InputStream inputStream) {

		PDDocument doc = null;
		try {
			doc = Loader.loadPDF(inputStream.readAllBytes());
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(doc);
		} catch (IOException e) {
			log.error("讀取 File 發生錯誤: ", e);

		}

		return null;
	}

	/**
	 * 讀取 pdf 文字檔 (存放在特定路徑)
	 * 
	 * @param file: pdf文檔
	 * @throws FileNotFoundException
	 */
	public static String readPdfFromPath(String path) {
		try {
			File file = new File(path);
			return readPdfContent(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			log.error("讀取文件失敗，文件不存在 ", e);
		}
		return null;
	}

}
