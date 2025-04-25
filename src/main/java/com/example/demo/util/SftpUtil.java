package com.example.demo.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SftpUtil {

	private static Session session; // 連線用的 Session
	private static ChannelSftp sftp; // SFTP 客戶端連線

	/**
	 * 帳號連線對方主機
	 * 
	 * @param host     Hostname
	 * @param port     port
	 * @param username 使用者名稱
	 * @param password 密碼
	 * @return FTPClient 連線後的 SFTPClient
	 */
	public static void connect(String host, int port, String username, String password) {
		try {
			JSch jsch = new JSch(); // 建立 SSH 連線。
			// 建立一個 Session
			session = jsch.getSession(username, host, port);
			session.setPassword(password); // 設置密碼
			// 創建一個 Properties 對象，用於配置連線屬性。
			Properties config = new Properties();
			// StrictHostKeyChecking=no: 表示忽略主機密鑰檢查，允許連接未被記錄的主機（安全性較低，僅用於開發或測試環境）。
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			log.info("SFTP session connected.");
			// 開啟 SFTP 通道，用於文件傳輸。
			Channel channel = session.openChannel("sftp");
			channel.connect();

			// 將通道對象轉換為 ChannelSftp 類型，便於操作 SFTP 特定功能（如文件上傳和下載）。
			sftp = (ChannelSftp) channel;
			log.info("connected to " + host);

		} catch (JSchException e) {
			log.error("SFTP 連線異常" + e.getMessage());
		}
	}

	/**
	 * 關閉連線
	 */
	public static void disconnect() {
		if (sftp != null && sftp.isConnected()) {
			sftp.disconnect();
		}
		if (session != null && session.isConnected()) {
			session.disconnect();
		}
		log.info("關閉連線");
	}

	/**
	 * 上傳文件
	 *
	 * @param remotePath 文件上傳路徑
	 * @param uploadFile 要上傳的文件
	 * @param filename   要上傳的文件名稱
	 */
	public static void upload(String remotePath, String uploadFile, String filename) {
		upload(remotePath, uploadFile + "/" + filename);
	}

	/**
	 * 上傳文件
	 *
	 * @param remotePath 文件上傳路徑
	 * @param uploadFile 要上傳的文件
	 */
	public static void upload(String remotePath, File uploadFile) {
		try {
			FileInputStream fileInputStream = new FileInputStream(uploadFile);
			sftp.put(fileInputStream, uploadFile.getName());
			// 關閉數據流
			fileInputStream.close();
			log.info("上傳文件成功");
		} catch (SftpException | IOException e) {
			log.error("發生錯誤，" + e.getMessage());
		}
	}

	/**
	 * 上傳文件
	 *
	 * @param remotePath 文件上傳路徑
	 * @param filePath   要上傳的文件路徑
	 */
	public static void upload(String remotePath, String filePath) {
		try {
			// 確認該路徑是否存在，若不存在建立它
			ensureRemoteDirectoryExists(remotePath);
			sftp.cd(remotePath); // 進入該路徑底下
			File file = new File(filePath);
			upload(remotePath, file);
			log.info("上傳文件成功");
		} catch (SftpException e) {
			log.error("上傳文件失敗，" + e.getMessage());
		}
	}

	/**
	 * 同步本地端目錄到遠端
	 *
	 * @param localDir  本地目錄
	 * @param remoteDir 遠端目錄
	 */
	public static void syncLocalToRemote(String localDir, String remoteDir) {
		try {
			// 確保遠端目錄存在，不存在遞迴建立它
			ensureRemoteDirectoryExists(remoteDir);

			// 獲取本地目錄
			File localDirectory = new File(localDir);
			if (!localDirectory.exists() || !localDirectory.isDirectory()) {
				log.error("本地目錄無效: " + localDir);
				throw new IllegalArgumentException("本地目錄無效: " + localDir);
			}
			// 查詢本地端資料清單
			File[] localFiles = localDirectory.listFiles();
			// 呼叫 ls 獲取遠端文件列表
			Vector<ChannelSftp.LsEntry> remoteEntries = sftp.ls(remoteDir);
			for (File localFile : localFiles) {
				if (localFile.isFile()) {
					// 同步文件
					syncSingleToRemote(localFile, remoteDir, remoteEntries);
				} else if (localFile.isDirectory()) {
					// 同步目錄遞迴
					String newRemoteDir = remoteDir + "/" + localFile.getName();
					syncLocalToRemote(localFile.getAbsolutePath(), newRemoteDir);
				}
			}
		} catch (SftpException e) {
			log.error("發生錯誤，" + e.getMessage());
		}
	}

	/**
	 * 同步單個文件到遠端
	 *
	 * @param localFile     本地文件
	 * @param remoteDir     遠端目錄
	 * @param remoteEntries 遠端目錄中的文件與目錄列表
	 */
	private static void syncSingleToRemote(File localFile, String remoteDir,
			Vector<ChannelSftp.LsEntry> remoteEntries) {
		String fileName = localFile.getName();
		// 判斷遠端是否已存在該文件
		boolean fileExistsOnRemote = remoteEntries.stream().anyMatch(entry -> entry.getFilename().equals(fileName));

		if (!fileExistsOnRemote) {
			// 文件不存在，執行上傳
			upload(remoteDir, localFile);
		} else {
			// 文件已存在，檢查是否需要更新
			ChannelSftp.LsEntry remoteFile = remoteEntries.stream()
					.filter(entry -> entry.getFilename().equals(fileName)).findFirst().orElse(null);

			// 若本地文件較新，執行更新
			if (remoteFile != null && localFile.lastModified() > remoteFile.getAttrs().getMTime() * 1000L) {
				upload(remoteDir, localFile);
			}
		}
	}

	/**
	 * 確保遠端目錄存在，若不存在則遞迴建立
	 *
	 * @param remotePath 目標路徑
	 * @throws SftpException 如果建立目錄失敗
	 */
	private static void ensureRemoteDirectoryExists(String remotePath) throws SftpException {
		try {
			sftp.cd(remotePath); // 嘗試進入目錄
		} catch (SftpException e) {
			log.info("目標路徑不存在，嘗試創建目錄：" + remotePath);
			// 對路徑字串根據"/"進行分割
			String[] directories = remotePath.split("/");

			String currentPath = "";
			for (String dir : directories) {
				if (dir.isEmpty()) {
					continue; // 跳過空節點（如:多個 '/'）
				}
				currentPath += "/" + dir;
				try {
					sftp.cd(currentPath); // 建立完畢後再嘗試進入
				} catch (SftpException ex) {
					// 目錄不存在，創建它
					sftp.mkdir(currentPath);
					sftp.cd(currentPath);
				}
			}
		}
	}

	/**
	 * 下載文件
	 * 
	 * @param directory    欲下載的目錄
	 * @param downloadFile 欲下載的文件
	 * @param savedPath    存在本地的路徑
	 */
	public static void download(String directory, String downloadFile, String savedPath) {
		String downloadFilePath = directory + "/" + downloadFile;
		download(downloadFilePath, savedPath);
		log.info("檔案下載成功，路徑為:" + savedPath);
	}

	/**
	 * 下載文件
	 * 
	 * @param downloadFilePath 欲下載的文件路徑
	 * @param savedPath        存在本地的路徑
	 */
	public static void download(String downloadFilePath, String savedPath) {
		try {
			File file = new File(savedPath);
			sftp.get(downloadFilePath, new FileOutputStream(file));
			log.info("檔案下載成功，路徑為" + savedPath);
		} catch (SftpException | FileNotFoundException e) {
			log.error("發生錯誤，" + e.getMessage());
		}
	}

	/**
	 * 同步遠端資料到本地端
	 * 
	 * @param remoteDir 遠端目錄
	 * @param localDir  本地目錄
	 */
	public static void syncRemoteToLocal(String remoteDir, String localDir) {
		ensureLocalDirectoryExists(localDir);
		// 獲取遠端目錄內容
		try {
			Vector<ChannelSftp.LsEntry> remoteEntries = sftp.ls(remoteDir);
			for (ChannelSftp.LsEntry entry : remoteEntries) {
				String entryName = entry.getFilename();
				log.info(entryName);
				// 忽略 "." 和 ".."
				if (StringUtils.equals(".", entryName) || StringUtils.equals("..", entryName)) {
					continue;
				}

				String remoteFilePath = remoteDir + "/" + entryName; // 遠端文件路徑
				String localFilePath = localDir + File.separator + entryName; // 本地端文件路徑
				if (entry.getAttrs().isDir()) {
					// 遞迴處理子目錄
					syncRemoteToLocal(remoteFilePath, localFilePath);
				} else {
					// 處理文件
					syncSingleToLocal(remoteFilePath, localFilePath, entry.getAttrs().getMTime());
				}
			}
		} catch (SftpException e) {
			log.error("同步遠端目錄到本地失敗， " + e.getMessage());
		}

	}

	/**
	 * 確保本地目錄存在
	 *
	 * @param localDir 本地目錄路徑
	 */
	private static void ensureLocalDirectoryExists(String localDir) {
		File directory = new File(localDir);
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			if (created) {
				log.info("已創建本地目錄: " + localDir);
			} else {
				log.error("無法創建本地目錄: " + localDir);
			}
		}
	}

	/**
	 * 同步單個文件到本地端
	 *
	 * @param remoteFilePath 遠端文件路徑
	 * @param localFilePath  本地文件路徑
	 * @param remoteFileTime 遠端文件修改時間
	 */
	private static void syncSingleToLocal(String remoteFilePath, String localFilePath, int remoteFileTime) {
		try {
			File localFile = new File(localFilePath);
			// 檢查是否需要下載文件
			if (localFile.exists() && localFile.lastModified() >= remoteFileTime * 1000L) {
				log.info("文件已是最新: " + localFilePath);
				return;
			}
			// 進行下載動作
			download(remoteFilePath, localFilePath);

			try (FileOutputStream fos = new FileOutputStream(localFile)) {
				sftp.get(remoteFilePath, fos);
				log.info("下載文件成功: " + remoteFilePath + " -> " + localFilePath);
			}
			// 設置本地文件的修改時間與遠端一致
			localFile.setLastModified(remoteFileTime * 1000L);
		} catch (Exception e) {
			log.error("下載文件失敗: " + remoteFilePath + " - " + e.getMessage());
		}
	}

}