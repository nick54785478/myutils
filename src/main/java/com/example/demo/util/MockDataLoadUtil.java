package com.example.demo.util;

import java.io.InputStream;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MockDataLoadUtil {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 讀取 JSON 成單一物件（適用單筆 JSON）
	 *
	 * @param path  resources 路徑，例如 "data/user.json"
	 * @param clazz 反序列化目標類型，例如 User.class
	 * @return T 解析後的物件
	 */
	public static <T> T loadJson(String path, Class<T> clazz) {
		log.info("開始載入 JSON（Class）：{}", path);

		try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {

			T result = objectMapper.readValue(inputStream, clazz);
			log.info("JSON 載入完成（Class）：{}", path);

			return result;

		} catch (Exception e) {
			log.error("讀取 JSON 時發生錯誤：{}", path, e);
			throw new RuntimeException("Failed to load JSON: " + path, e);
		}
	}

	/**
	 * 讀取 JSON 成泛型集合（如：List<T>、Map<String,T>）
	 *
	 * @param path resources 路徑，例如 "data/list.json"
	 * @param type 目標 TypeReference，例如 new TypeReference<List<User>>() {}
	 * @return T 泛型結果物件（可為 List / Map / Set ...）
	 */
	public static <T> T loadJson(String path, TypeReference<T> type) {
		log.info("開始載入 JSON（TypeReference）：{}", path);

		try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {

			T result = objectMapper.readValue(inputStream, type);
			log.info("JSON 載入完成（TypeReference）：{}", path);

			return result;

		} catch (Exception e) {
			log.error("讀取 JSON 時發生錯誤：{}", path, e);
			throw new RuntimeException("Failed to load JSON: " + path, e);
		}
	}
}
