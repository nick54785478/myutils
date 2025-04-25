package com.example.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Html2PdfUtil {

	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	public static void main(String[] args) {
		try {
			testFlyingSaucer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 測試
	 */
	public static void testFlyingSaucer() throws Exception {

		String htmlFilePath = System.getProperty("user.dir") + "/src/main/resources" + "/html/quotation.html"; // 靜態資源路徑

		// Read HTML content from file
		String htmlContent = new String(Files.readAllBytes(Paths.get(htmlFilePath)));

		String outputPdfFilePath = System.getProperty("user.dir") + "/src/main/resources" + "/result/quotation.pdf"; // 靜態資源路徑

		ByteArrayOutputStream byteArrayOutputStream = convertHtmlToPdf(htmlContent);

		// Create an output stream to write the PDF
		try (OutputStream outputStream = new FileOutputStream(outputPdfFilePath)) {
			byteArrayOutputStream.writeTo(outputStream);

		} catch (Exception e) {
			System.out.println("Error");
		}

	}

	/**
	 * 轉換 Html 為 pdf
	 * 
	 * @param html 內容
	 * @return ByteArrayOutputStream
	 */
	public static ByteArrayOutputStream convertHtmlToPdf(String htmlContent) throws Exception {

		// Create an ITextRenderer instance
		ITextRenderer renderer = new ITextRenderer();

		// 設置字體路徑
		String fontPath = RESOURCE + "/fonts/simsun.ttf"; // 載入你自己的字體文件
		System.out.println("字體路徑: " + RESOURCE + "/fonts/simsun.ttf");

		// 設置字體
		ITextFontResolver fontResolver = renderer.getFontResolver();
		fontResolver.addFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // *** 很重要 -> BaseFont.EMBEDDED
																				// 將字體嵌入到PDF中時

		// Set the base URL to resolve relative URLs
		renderer.setDocumentFromString(htmlContent);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		// Generate the PDF
		renderer.layout();
		renderer.createPDF(outputStream);

		// 新增頁碼
		addPageNumber(new PdfReader(outputStream.toByteArray()), outputStream);

		return outputStream;
	}

	/**
	 * pdf添加頁碼
	 *
	 * @param inputStream
	 * @param outputStream
	 */
	public static void addPageNumber(PdfReader reader, OutputStream outputStream) throws Exception {
		// PdfReader reader = new PdfReader(inputStream);
		Document document = new Document(reader.getPageSizeWithRotation(1));
		// 複製原pdf檔第一頁建立新的pdf
		PdfCopy pdfNew = new PdfCopy(document, outputStream);

		document.open();
		PdfImportedPage page = null;

		int pagesNum = 0;// 總頁碼
		int pages = 0; // 當前頁碼

		pagesNum += reader.getNumberOfPages();

		// 插入頁碼必需不用頁碼可刪除
		PdfCopy.PageStamp stamp;

		for (int i = 1; i <= reader.getNumberOfPages(); i++) {
			pages++;
			document.newPage();
			page = pdfNew.getImportedPage(reader, i);
			stamp = pdfNew.createPageStamp(page);// 插入頁碼必需的，不要可刪除

			ColumnText.showTextAligned(stamp.getUnderContent(), Element.ALIGN_CENTER,
					new Phrase(addFont(String.format("000001", pages, pagesNum), 10f)), 52f, 10f, 0f);// x:52f , y: 10f,
																										// rotation: 0f

			ColumnText.showTextAligned(stamp.getUnderContent(), Element.ALIGN_CENTER,
					new Phrase(addFont(String.format("Page: %d/%d", pages, pagesNum), 10f)), 520f, 10f, 0f);// 插入頁碼必需的，不要可刪除
			stamp.alterContents();// 插入頁碼必需的，不要可刪除
			pdfNew.addPage(page);
		}

		// close everything
		document.close();
		pdfNew.close();
		reader.close();
	}

	/**
	 * 將字體樣式加入正文中。
	 *
	 * @param content 字體內容
	 * @param size    字體大小
	 * @return 新增了字體樣式的正文
	 */
	private static Paragraph addFont(String content, float size) {
		// 註冊字體
		FontFactory.register(RESOURCE + "/fonts/simsun.ttf", "simsun");
		Font font = FontFactory.getFont("simsun", BaseFont.IDENTITY_H, BaseFont.EMBEDDED); // 設置字體
		return addText(content, font);
	}

	/**
	 * 建立一個包含指定內容和字體樣式的段落。
	 * 
	 * @param content 段落內容
	 * @param font    字型
	 * @return 包含指定內容和字體樣式的段落
	 */
	private static Paragraph addText(String content, Font font) {
		Paragraph paragraph = new Paragraph(content, font); // 建立一個段落對象，並設定內容和字體樣式
		paragraph.setAlignment(Element.ALIGN_LEFT); // 設定橫向對齊方式為左對齊
		return paragraph;
	}

}