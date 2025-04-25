package com.example.demo.util;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 物件解析工具
 * */
@Log4j2
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassParseUtil {

	protected static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * 序列化物件 為 JSON 字串
	 * 
	 * @param target 目標物件
	 * @return String 序列化後的 json 字串
	 */
	public static String serialize(Object target) {
		try {
			return mapper.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			log.error("Occurred Json Processing Exception", e);
			return "";
		}
	}
	
	/**
	 * 序列化物件 為 byte[]
	 * 
	 * @param target 目標物件
	 * @return 物件序列化後的 byte[]
	 */
	public static byte[] serializeAsBytes(Object target) {
		try {
			return mapper.writeValueAsBytes(target);
		} catch (JsonProcessingException e) {
			log.error("Occurred Json Processing Exception", e);
			return new byte[0];
		}
	}
	
	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target byte[]
	 * @param clazz  目標物件類型
	 * @return 物件
	 */
	public static <T> T unserialize(byte[] target, Class<T> clazz) {
		try {
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (IOException e) {
			log.error("Occurred ByteProcessing Exception", e);
			return null;
		}
	}
	
	/**
	 * 反序列化 JSON 回 物件
	 * 
	 * @param target json 字串
	 * @param clazz  目標物件類型
	 * @return 物件
	 */
	public static <T> T unserialize(String target, Class<T> clazz) {
		try {
			return mapper.readValue(target, clazz);
		} catch (JsonMappingException e) {
			log.error("Occurred JsonMapping Exception", e);
			return null;
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}

	/**
	 * 反序列化 JSON 回 物件列表
	 * 
	 * @param target json 字串
	 * @param clazz  目標物件類型
	 * @return 物件列表
	 */
	public static <T> List<T> unserializeArrayOfObject(String target, Class<T> clazz) {
		try {
			return mapper.readValue(target, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
		} catch (JsonProcessingException e) {
			log.error("Occurred JsonProcessing Exception", e);
			return null;
		}
	}
}
