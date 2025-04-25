package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipUtils {

	/**
	 * 打包為zip
	 * 
	 * @param btye[]
	 * @param fileName
	 * @param zipName
	 */
	public static File packToZip(byte[] data, String fileName, String zipFilepath) throws IOException {
		File zipFile = new File(zipFilepath);
		try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
			addToZipFile(zos, fileName, data);

		}
		return zipFile;
	}

	/**
	 * 打包為zip
	 * 
	 * @param byte[]   collection map
	 * @param filename 檔案名稱(含路徑)
	 */
	public static File packToZip(Map<String, byte[]> map, String fileName) throws IOException {
		File zipFile = new File(fileName);
//        File zipFile = File.createTempFile(fileName); // 創建一個臨時文件

		try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos)) {
			map.forEach((k, v) -> {
				try {
					addToZipFile(zos, k, v);
				} catch (IOException e) {
					log.error("發生錯誤", e);
				}
			});
		} finally {
			if (zipFile != null) {
//                zipFile.delete(); // 手動移除該臨時文件
			}
		}
		return zipFile;
	}

	/**
	 * 將 byte[] 加入 zip
	 * 
	 * @throws IOException
	 */
	private static void addToZipFile(ZipOutputStream zos, String fileName, byte[] data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		// 將位元組寫入 ZipOutputStream
		zos.write(data);
		zos.closeEntry();
	}

	/**
	 * 讀取 inputStream
	 */
	public static byte[] readPdfFile(String filePath) throws IOException {
		File pdfFile = new File(filePath);
		byte[] data = new byte[(int) pdfFile.length()];
		try (FileInputStream fis = new FileInputStream(pdfFile)) {
			fis.read(data);
		}
		return data;
	}

	public static void main(String[] args) {
		try {
			byte[] pdfFile = readPdfFile(System.getProperty("user.home") + "/Downloads/report1.pdf");
			byte[] pdfFile1 = readPdfFile(System.getProperty("user.home") + "/Downloads/email.pdf");
			Map<String, byte[]> map = new HashMap<>();

			map.put("report1.pdf", pdfFile);
			map.put("email.pdf", pdfFile1);

			packToZip(map, System.getProperty("user.home") + "/Downloads/download.zip");
			System.out.println("下載成功 zip");
		} catch (Exception e) {
			log.error("發生錯誤", e);

		}
	}
}
