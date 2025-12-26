package com.example.demo.util;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 工具類：提供 InputStream 與 常用格式之間的轉換。
 * <p>
 * 功能涵蓋：
 * <ul>
 *     <li>InputStream 與 byte[]、String、File、Base64 之間的轉換</li>
 *     <li>InputStream 的 clone（記憶體內 / 臨時檔案，大檔案安全版）</li>
 *     <li>圖片相關轉換：InputStream ↔ BufferedImage</li>
 * </ul>
 * <p>
 * 特點：
 * <ul>
 *     <li>避免重複編寫 I/O 處理邏輯</li>
 *     <li>提供大檔案 clone 方法，避免記憶體溢位</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InputStreamUtil {

    private static final int BUFFER_SIZE = 8192; // 緩衝區大小


    /**
     * 將 {@link InputStream} 轉換為 byte[]。
     *
     * @param inputStream 輸入串流
     * @return 轉換後的 byte[]
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[BUFFER_SIZE];
            int nRead;
            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    /**
     * 將 {@link InputStream} 轉換為字串（使用 UTF-8 編碼）。
     *
     * @param inputStream 輸入串流
     * @return 轉換後的字串
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static String toString(InputStream inputStream) throws IOException {
        return toString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 將 {@link InputStream} 轉換為字串（指定編碼）。
     *
     * @param inputStream 輸入串流
     * @param charset     指定字元編碼
     * @return 轉換後的字串
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static String toString(InputStream inputStream, Charset charset) throws IOException {
        return new String(toByteArray(inputStream), charset);
    }

    /**
     * 將 {@link InputStream} 寫入指定檔案。
     *
     * @param inputStream 輸入串流
     * @param filePath    輸出檔案路徑
     * @return 寫入完成的 {@link File} 物件
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static File toFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        try (OutputStream outStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }
        return file;
    }

    /**
     * 將 {@link InputStream} 轉換為 Base64 編碼字串。
     *
     * @param inputStream 輸入串流
     * @return Base64 字串
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static String toBase64(InputStream inputStream) throws IOException {
        return Base64.getEncoder().encodeToString(toByteArray(inputStream));
    }

    /**
     * 將 byte[] 轉換為 {@link InputStream}。
     *
     * @param data 位元組陣列
     * @return 對應的 InputStream
     */
    public static InputStream fromByteArray(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    /**
     * 將字串轉換為 {@link InputStream}（UTF-8 編碼）。
     *
     * @param str 字串內容
     * @return 對應的 InputStream
     */
    public static InputStream fromString(String str) {
        return new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Clone InputStream（記憶體內版本）。
     * <p>
     * 適合小檔案，會將整個 InputStream 載入記憶體。
     *
     * @param inputStream 輸入串流
     * @param copies      需要幾份 clone
     * @return 多個可重複讀取的 InputStream
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static InputStream[] clone(InputStream inputStream, int copies) throws IOException {
        byte[] bytes = toByteArray(inputStream);
        InputStream[] streams = new InputStream[copies];
        for (int i = 0; i < copies; i++) {
            streams[i] = new ByteArrayInputStream(bytes);
        }
        return streams;
    }

    /**
     * Clone InputStream（單份，記憶體內）。
     *
     * @param inputStream 輸入串流
     * @return 新的 InputStream
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static InputStream clone(InputStream inputStream) throws IOException {
        return new ByteArrayInputStream(toByteArray(inputStream));
    }

    /**
     * 安全 Clone InputStream（適合大檔案）。
     * <p>
     * 會將 InputStream 寫入臨時檔案，再建立多個 {@link FileInputStream}。
     * 注意：呼叫端需自行關閉這些 InputStream。
     *
     * @param inputStream 輸入串流
     * @param copies      需要幾份 clone
     * @return 多個 FileInputStream
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static InputStream[] cloneLarge(InputStream inputStream, int copies) throws IOException {
        // 建立臨時檔案（JVM 結束時刪除）
        File tempFile = File.createTempFile("inputstream_clone_", ".tmp");
        tempFile.deleteOnExit();

        // 將原始 InputStream 寫入臨時檔案
        try (OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }

        // 為每個 clone 建立新的 FileInputStream
        InputStream[] streams = new InputStream[copies];
        for (int i = 0; i < copies; i++) {
            streams[i] = new FileInputStream(tempFile);
        }
        return streams;
    }


    /**
     * 將 {@link InputStream} 轉換為 {@link BufferedImage}。
     *
     * @param inputStream 輸入串流
     * @return 對應的 BufferedImage（若格式不支援可能回傳 null）
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static BufferedImage toBufferedImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    /**
     * 將 {@link BufferedImage} 轉換為 byte[]。
     *
     * @param image      圖片物件
     * @param formatName 格式名稱（如 "png", "jpg"）
     * @return 圖片資料的 byte[]
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static byte[] fromBufferedImage(BufferedImage image, String formatName) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, baos);
            return baos.toByteArray();
        }
    }

    /**
     * 將 {@link BufferedImage} 轉換為 {@link InputStream}。
     *
     * @param image      圖片物件
     * @param formatName 格式名稱（如 "png", "jpg"）
     * @return 對應的 InputStream
     * @throws IOException 發生 I/O 錯誤時拋出
     */
    public static InputStream bufferedImageToInputStream(BufferedImage image, String formatName) throws IOException {
        return new ByteArrayInputStream(fromBufferedImage(image, formatName));
    }
}
