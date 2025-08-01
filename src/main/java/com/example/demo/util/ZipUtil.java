package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipUtil {

	/**
	 * 將單一 byte[] 資料打包為 zip
	 *
	 * @param data     資料流
	 * @param fileName 檔案名稱
	 * @return 壓縮後的 zip Resource
	 */
	public static ByteArrayResource packFileToZipFromBytes(byte[] data, String fileName) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			addToZipFile(zipOutputStream, fileName, data);
		} catch (Exception e) {
			log.error("發生錯誤", e);
		}
		return new ByteArrayResource(byteArrayOutputStream.toByteArray());
	}

	/**
	 * 將單一 byte[] 資料打包為加密的 zip
	 *
	 * @param data     原始檔案的位元組資料
	 * @param fileName 壓縮檔中顯示的檔名
	 * @param password 加密密碼
	 * @return 加密壓縮後的 ByteArrayResource
	 * @throws IOException 壓縮或讀取過程中出錯時拋出
	 */
	public static ByteArrayResource packFileToZipFromBytes(byte[] data, String fileName, String password)
			throws IOException {
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
		return packFileToZipFromInputStream(byteArrayInputStream, fileName, password);
	}

	/**
	 * 將單一 InputStream 資料打包為 zip
	 *
	 * @param inputStream 資料流
	 * @param fileName    檔案名稱
	 * @return 壓縮後的 zip Resource
	 */
	public static ByteArrayResource packFileToZipFromInputStream(InputStream inputStream, String fileName)
			throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			addToZipFile(zipOutputStream, fileName, inputStream);
		} catch (Exception e) {
			log.error("發生錯誤", e);
		}
		return new ByteArrayResource(byteArrayOutputStream.toByteArray());
	}

	/**
	 * 將單一 InputStream 資料打包為加密的 zip
	 *
	 * @param inputStream 原始檔案的資料流
	 * @param fileName    壓縮檔中顯示的檔名
	 * @param password    加密密碼
	 * @return 加密壓縮後的 ByteArrayResource
	 * @throws IOException 壓縮或讀取過程中出錯時拋出
	 */
	public static ByteArrayResource packFileToZipFromInputStream(InputStream inputStream, String fileName,
			String password) throws IOException {
		File tempZipFile = File.createTempFile("secure-", ".zip");
		try (ZipFile zipFile = new ZipFile(tempZipFile, password.toCharArray())) {
			ZipParameters parameters = generateZipParameters(fileName, EncryptionMethod.AES);

			// 將輸入資料流壓縮進 zip
			zipFile.addStream(inputStream, parameters);

			// 讀出壓縮檔為 byte[]
			byte[] zipBytes = Files.readAllBytes(tempZipFile.toPath());

			// 回傳為 Spring ByteArrayResource（通常可直接用於下載）
			return new ByteArrayResource(zipBytes);
		} catch (ZipException e) {
			throw new IOException("Zip creation failed", e);
		} finally {
			Files.deleteIfExists(tempZipFile.toPath());
		}
	}

	/**
	 * 將多筆 byte[] 資料打包為 zip
	 *
	 * @param dataMap Map<檔案名稱, byte[]>
	 */
	public static ByteArrayResource packFilesToZipFromBytes(Map<String, byte[]> dataMap) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			dataMap.forEach((k, v) -> {
				try {
					addToZipFile(zipOutputStream, k, v);
				} catch (IOException e) {
					log.error("加入檔案 [{}] 到 zip 時發生錯誤", k, e);
				}
			});
		} catch (IOException e) {
			log.error("產生 zip 壓縮檔時發生錯誤", e);
		}
		return new ByteArrayResource(byteArrayOutputStream.toByteArray());
	}

	/**
	 * 將多筆 byte[] 資料打包為加密的 zip
	 *
	 * @param files    Map<檔案名稱, byte[]>
	 * @param password 加密密碼
	 * @return 加密壓縮後的 ByteArrayResource
	 * @throws IOException 壓縮或讀取過程中出錯時拋出
	 */
	public static ByteArrayResource packFilesToZipFromBytes(Map<String, byte[]> files, String password)
			throws IOException {
		File tempZipFile = File.createTempFile("secure-multi-", ".zip");
		try (ZipFile zipFile = new ZipFile(tempZipFile, password.toCharArray())) {
			for (Map.Entry<String, byte[]> entry : files.entrySet()) {
				ZipParameters parameters = generateZipParameters(entry.getKey(), EncryptionMethod.AES);
				zipFile.addStream(new ByteArrayInputStream(entry.getValue()), parameters);
			}
		} catch (ZipException e) {
			throw new IOException("Failed to create encrypted zip", e);
		}

		ByteArrayResource result = new ByteArrayResource(Files.readAllBytes(tempZipFile.toPath()));
		Files.delete(tempZipFile.toPath()); // 使用 Files.delete 以取得更明確的錯誤
		return result;
	}

	/**
	 * 將多筆資料 (InputStream) 打包為 zip
	 *
	 * @param dataMap Map<String, InputStream>
	 */
	public static ByteArrayResource packFilesToZipFromInputStream(Map<String, InputStream> dataMap) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
			dataMap.forEach((k, v) -> {
				try {
					addToZipFile(zipOutputStream, k, v);
				} catch (IOException e) {
					log.error("加入檔案 [{}] 到 zip 時發生錯誤", k, e);
				}
			});
		} catch (IOException e) {
			log.error("產生 zip 壓縮檔時發生錯誤", e);
		}
		return new ByteArrayResource(byteArrayOutputStream.toByteArray());
	}

	/**
	 * 將多筆 InputStream 資料打包為加密的 zip
	 *
	 * @param files    Map<檔案名稱, InputStream>
	 * @param password 加密密碼
	 * @return 加密壓縮後的 ByteArrayResource
	 * @throws IOException 壓縮或讀取過程中出錯時拋出
	 */
	public static ByteArrayResource packFilesToZipFromInputStream(Map<String, InputStream> files, String password)
			throws IOException {
		File tempZipFile = File.createTempFile("secure-multi-", ".zip");
		try (ZipFile zipFile = new ZipFile(tempZipFile, password.toCharArray())) {
			for (Map.Entry<String, InputStream> entry : files.entrySet()) {
				ZipParameters parameters = generateZipParameters(entry.getKey(), EncryptionMethod.AES);
				zipFile.addStream(entry.getValue(), parameters);
			}
		} catch (ZipException e) {
			throw new IOException("Failed to create encrypted zip", e);
		}

		ByteArrayResource result = new ByteArrayResource(Files.readAllBytes(tempZipFile.toPath()));
		Files.delete(tempZipFile.toPath()); // 使用 Files.delete 以取得更明確的錯誤
		return result;
	}

	/**
	 * 將 byte[] 寫入 ZipOutputStream
	 *
	 * @param zos      ZipOutputStream
	 * @param fileName 檔案名稱
	 * @param data     byte[]
	 */
	private static void addToZipFile(ZipOutputStream zos, String fileName, byte[] data) throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		// 將位元組寫入 ZipOutputStream
		zos.write(data);
		zos.closeEntry();
	}

	/**
	 * 將 InputStream 寫入 ZipOutputStream
	 *
	 * @param zos         ZipOutputStream
	 * @param fileName    檔案名稱
	 * @param inputStream InputStream 檔案流
	 */
	private static void addToZipFile(ZipOutputStream zos, String fileName, InputStream inputStream) throws IOException {
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);
		// 將位元組寫入 ZipOutputStream
		byte[] buffer = new byte[4096];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			zos.write(buffer, 0, length);
		}
		zos.closeEntry();
	}

	/**
	 * 讀取檔案為 byte[]（支援任意檔案）
	 *
	 * @param filePath 檔案路徑
	 * @return byte[]
	 */
	public static byte[] readPdfFile(String filePath) throws IOException {
		File file = new File(filePath);
		try (FileInputStream fis = new FileInputStream(file)) {
			return fis.readAllBytes();
		}
	}

	/**
	 * 建立 Zip Parameters
	 * 
	 * @param fileName         檔案名稱
	 * @param encryptionMethod 加密方式
	 * @return ZipParameters
	 */
	private static ZipParameters generateZipParameters(String fileName, EncryptionMethod encryptionMethod) {
		ZipParameters parameters = new ZipParameters();
		parameters.setFileNameInZip(fileName);
		parameters.setEncryptFiles(true);
		parameters.setEncryptionMethod(encryptionMethod);
		return parameters;
	}

}
