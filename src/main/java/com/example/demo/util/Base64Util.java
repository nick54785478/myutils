package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Base64Util {

	/**
	 * 將 InputStream 編碼為 Base64 字串
	 *
	 * @param inputStream 資料流
	 * @return Base64 字串
	 */
	public static String encode(InputStream inputStream) {
		try {
			byte[] bytes = inputStream.readAllBytes();
			return Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 將 Base64 字串轉回 InputStream
	 *
	 * @param base64String Base64 字串
	 * @return InputStream
	 */
	public static InputStream decodeToInputStream(String base64String) {
		byte[] decodedBytes = Base64.getDecoder().decode(base64String);
		return new ByteArrayInputStream(decodedBytes);
	}

	/**
	 * 編碼為 Base64 字串
	 *
	 * @param plainText 原始內容
	 * @return Base64 字串
	 */
	public static String encode(String plainText) {
		return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 解碼 Base64 字串回原始內容
	 *
	 * @param base64Encoded Base64 字串
	 * @return 原始內容
	 */
	public static String decode(String base64Encoded) {
		return new String(Base64.getDecoder().decode(base64Encoded), StandardCharsets.UTF_8);
	}

	/**
	 * 組合字串編碼並 Base64 編碼（用於 Basic Auth）
	 *
	 * @param clientId     客戶端 ID
	 * @param clientSecret 客戶端密鑰
	 * @return Base64 編碼
	 */
	public static String encodeBasicAuth(String clientId, String clientSecret) {
		String credentials = clientId + ":" + clientSecret;
		return "Basic " + encode(credentials);
	}
}
