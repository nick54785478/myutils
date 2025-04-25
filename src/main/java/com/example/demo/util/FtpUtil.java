package com.example.demo.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Ftp Util
 * 使用 FTP 方式於兩台主機之間傳輸檔案
 */
@Log4j2
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FtpUtil {


	/**
	 * 帳號連線對方主機
	 * 
	 * @param ftpClient
	 * @param server    FTP 伺服器 IP
	 * @param port      port
	 * @param username  使用者名稱
	 * @param password  密碼
	 */
	public static FTPClient connect(FTPClient ftpClient, String server, int port, String username, String password)
			throws IOException {
		// 連接到 FTP 伺服器
		ftpClient.connect(server, port);
		ftpClient.setControlEncoding("UTF-8");
		ftpClient.setConnectTimeout(10000); // 連線超時時間
		ftpClient.setAutodetectUTF8(true);
		ftpClient.setBufferSize(1024 * 1024);
		ftpClient.enterLocalPassiveMode(); // 設置文件傳輸模式 - 被動模式

		int replyCode = ftpClient.getReplyCode();
		// 拒絕連線
		if (!FTPReply.isPositiveCompletion(replyCode)) {
			log.error("FTP server refused connection.");
			ftpClient.disconnect();
			return null;
		}

		ftpClient.login(username, password);
		// 登入
		boolean success = ftpClient.login(username, password);
		if (!success) {
			log.error("Login Fail!");
			return null;
		}
		log.info("Login successfully.");

		// 設定檔案類型（二進制檔案）
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		return ftpClient;
	}

	/**
	 * 關閉連線
	 * 
	 * @param ftpClient
	 */
	public static void closeConect(FTPClient ftpClient) {
		// 確保斷開連線
		try {
			if (!ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (IOException e) {
			log.error("關閉連線時發生 IO Exception");
		}
	}

	/**
	 * 上傳動作
	 * 
	 * @param ftpClient
	 * @param localFilePath 本地端檔案路徑
	 * @param remotePath    上傳路徑
	 * @param filename      文件名
	 */
	public static void upload(FTPClient ftpClient, String localFilePath, String remotePath, String filename) {
		try {
			String filePath = remotePath + "/" + filename; // 組合 File Path
			// 確保遠端目錄存在，如果路徑不存在建立一個
			ensureRemoteDirectoryExists(ftpClient, remotePath);
			// 開始上傳資料
			ftpClient.storeFile(filePath, new FileInputStream(localFilePath));
			log.info("upload successfully !");
		} catch (FileNotFoundException e) {
			log.error("發生錯誤: " + e.getMessage());
		} catch (IOException e) {
			log.error("發生錯誤； " + e.getMessage());
		} finally {
			closeConect(ftpClient);
		}
	}

	/**
	 * 同步本地目錄與遠端目錄
	 *
	 * @param ftpClient          FTP 客戶端
	 * @param localDirPath       本地目錄
	 * @param remoteDirPath      遠端目錄
	 * @param deleteRemoteExtras 是否刪除遠端多餘檔案
	 */
	public static void sync(FTPClient ftpClient, String localDirPath, String remoteDirPath, String deleteRemoteExtras) {

		File localDir = new File(localDirPath);
		if (!localDir.isDirectory()) {
			log.error("本地路徑必須是一個目錄: " + localDirPath);
			throw new IllegalArgumentException("本地路徑必須是一個目錄: " + localDirPath);
		}

		// 確保遠端目錄存在，若不存在就建立
		try {
			ensureRemoteDirectoryExists(ftpClient, remoteDirPath);
			// 取得本地與遠端檔案清單
			Set<String> localFiles = new HashSet<>(Arrays.asList(localDir.list()));
			FTPFile[] remoteFiles = ftpClient.listFiles(remoteDirPath); // 列出遠端資料清單
			Set<String> remoteFileNames = new HashSet<>();

			for (FTPFile remoteFile : remoteFiles) {
				remoteFileNames.add(remoteFile.getName());
			}
			log.info("本地檔案清單: {}", localFiles);
			log.info("遠端檔案清單: {}", remoteFileNames);

			// 上傳本地不存在於遠端的檔案
			for (String localFileName : localFiles) {
				File localFile = new File(localDirPath + "/" + localFileName);
				if (localFile.isDirectory()) {
					System.out.println("localFile:" + localFileName);
					// 遞迴處理子目錄
					sync(ftpClient, localFile.getAbsolutePath(), remoteDirPath + "/" + localFileName,
							deleteRemoteExtras);
				} else if (!remoteFileNames.contains(localFileName)) {
					// 若遠端清單不包含該檔名，進行檔案上傳
					upload(ftpClient, localFile.getAbsolutePath(), remoteDirPath, localFileName);
				}
			}

			// 處理遠端檔案：刪除本地不存在的檔案
			if (StringUtils.equals("Y", deleteRemoteExtras)) {
				for (FTPFile remoteFile : remoteFiles) {
					String localFilePath = localDir + "/" + remoteFile.getName();
					File localFile = new File(localFilePath);

					if (!localFile.exists()) {
						String remoteFilePath = remoteDirPath + "/" + remoteFile.getName();
						if (remoteFile.isDirectory()) {
							// 遞迴刪除遠端目錄
							deleteRemoteDirectory(ftpClient, remoteFilePath);
						} else {
							// 刪除遠端檔案
							boolean deleted = ftpClient.deleteFile(remoteFilePath);
							if (deleted) {
								log.warn("刪除遠端檔案: " + remoteFilePath);
							} else {
								log.error("刪除遠端檔案失敗: " + remoteFilePath);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			log.error("發生 IO Exception，同步失敗");
		}

	}

	/**
	 * 確保遠端目錄存在，若不存在則建立
	 * 
	 * @param ftpClient  FTP 客戶端
	 * @param remotePath 目標遠端目錄路徑
	 * @throws IOException 如果操作失敗
	 */
	private static void ensureRemoteDirectoryExists(FTPClient ftpClient, String remotePath) throws IOException {
		String[] pathElements = remotePath.split("/");
		StringBuilder currentPath = new StringBuilder();

		for (String folder : pathElements) {
			// 若為空，跳過
			if (folder.isEmpty()) {
				continue;
			}

			// 加入路徑
			currentPath.append("/").append(folder);
			String directory = currentPath.toString();

			// 嘗試切換到目錄
			if (!ftpClient.changeWorkingDirectory(directory)) {
				// 如果目錄不存在，建立目錄
				boolean created = ftpClient.makeDirectory(directory);
				if (created) {
					log.info("建立目錄: {}", directory);
				} else {
					log.error("無法建立目錄: {}", directory);
					throw new IOException("Failed to create directory: " + directory);
				}
			}
		}
	}

	/**
	 * 遞迴刪除遠端目錄
	 *
	 * @param ftpClient FTP 客戶端
	 * @param remoteDir 遠端目錄
	 * @throws IOException 如果操作失敗
	 */
	private static void deleteRemoteDirectory(FTPClient ftpClient, String remoteDir) throws IOException {
		FTPFile[] remoteFiles = ftpClient.listFiles(remoteDir);

		for (FTPFile remoteFile : remoteFiles) {
			String remoteFilePath = remoteDir + "/" + remoteFile.getName();
			if (remoteFile.isDirectory()) {
				deleteRemoteDirectory(ftpClient, remoteFilePath);
			} else {
				ftpClient.deleteFile(remoteFilePath);
			}
		}
		ftpClient.removeDirectory(remoteDir);
		log.warn("刪除遠端目錄: {}", remoteDir);
	}
}