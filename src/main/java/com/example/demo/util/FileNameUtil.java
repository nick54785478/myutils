package com.example.demo.util;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileNameUtil {

	/**
	 * 移除開頭的 prefix（第一段斜線前）
	 *
	 * @param fullPath         完整路徑
	 * @param keepLeadingSlash 是否保留 "/"
	 * @return 移除後的字串
	 */
	public static String removePrefix(String fullPath, boolean keepLeadingSlash) {
		if (fullPath == null || !fullPath.contains("/")) {
			return fullPath;
		}
		int index = keepLeadingSlash ? fullPath.indexOf("/") : fullPath.indexOf("/") + 1;
		return fullPath.substring(index);
	}

	/**
	 * 移除尾端檔案名稱，僅保留資料夾路徑
	 *
	 * @param fullPath          完整路徑
	 * @param keepTrailingSlash 是否保留結尾 "/"
	 * @return 父層資料夾路徑
	 */
	public static String getParentPath(String fullPath, boolean keepTrailingSlash) {
		if (fullPath == null || !fullPath.contains("/")) {
			return "";
		}
		int lastSlashIndex = fullPath.lastIndexOf('/');
		if (keepTrailingSlash) {
			lastSlashIndex += 1;
		}
		return fullPath.substring(0, lastSlashIndex);
	}

	/**
	 * 從路徑中移除指定 segment（例如資料夾名稱、關鍵字）
	 *
	 * @param fullPath 原始完整路徑
	 * @param segment  要移除的片段（不包含斜線）
	 * @return 移除後的新路徑
	 */
	public static String removeSegment(String fullPath, String segment) {
		if (fullPath == null || segment == null || segment.isEmpty()) {
			return fullPath;
		}
		String[] parts = fullPath.split("/+");
		List<String> filtered = Arrays.stream(parts).filter(part -> !part.equals(segment)).collect(Collectors.toList());
		return String.join("/", filtered);
	}

	/**
	 * 替換指定 segment 為新字串（比對單一 segment）
	 *
	 * @param fullPath    原始路徑
	 * @param target      要被替換的 segment
	 * @param replacement 替換後的新 segment
	 * @return 替換後路徑
	 */
	public static String replaceSegment(String fullPath, String target, String replacement) {
		if (fullPath == null || target == null || replacement == null) {
			return fullPath;
		}
		String[] parts = fullPath.split("/+");
		List<String> replaced = Arrays.stream(parts).map(part -> part.equals(target) ? replacement : part)
				.collect(Collectors.toList());
		return String.join("/", replaced);
	}

	/**
	 * 查詢 fullPath 的所有 segment，回傳其 index 對應 map
	 *
	 * @param fullPath 完整路徑
	 * @return Map<index, segment>
	 */
	public static Map<Integer, String> checkIndex(String fullPath) {
		Map<Integer, String> map = new LinkedHashMap<>();
		if (fullPath == null || fullPath.isEmpty()) {
			return map;
		}
		String[] parts = fullPath.split("/+");
		for (int i = 0; i < parts.length; i++) {
			map.put(i, parts[i]);
		}
		return map;
	}

	/**
	 * 根據 Map<index, value> 替換 fullPath 中對應位置的 segment
	 *
	 * @param fullPath     原始路徑
	 * @param replacements 要替換的 index 與值
	 * @return 替換後路徑
	 */
	public static String replaceSegmentsByIndex(String fullPath, Map<Integer, String> replacements) {
		if (fullPath == null || replacements == null || replacements.isEmpty()) {
			return fullPath;
		}
		String[] parts = fullPath.split("/+");
		for (Map.Entry<Integer, String> entry : replacements.entrySet()) {
			int index = entry.getKey();
			String newVal = entry.getValue();
			if (index >= 0 && index < parts.length && newVal != null) {
				parts[index] = newVal;
			}
		}
		return String.join("/", parts);
	}

	/**
	 * 從 Map<index, segment> 建立完整路徑（依 index 遞增排序）
	 *
	 * @param segments Map<index, segment>
	 * @return 組合的路徑字串
	 */
	public static String buildPathFromMap(Map<Integer, String> segments) {
		if (segments == null || segments.isEmpty()) {
			return "";
		}
		return String.join("/", new TreeMap<>(segments).values());
	}

	/**
	 * 取得指定 index 的 segment 值
	 *
	 * @param fullPath 路徑字串
	 * @param index    要查詢的 segment index
	 * @return segment 值，若超出範圍則回傳 null
	 */
	public static String getSegmentAtIndex(String fullPath, int index) {
		if (fullPath == null || index < 0)
			return null;
		String[] parts = fullPath.split("/+");
		if (index >= parts.length)
			return null;
		return parts[index];
	}

	/**
	 * 組合路徑，會自動清理多餘的斜線，忽略 null 與空字串。
	 *
	 * @param segments 路徑片段（不限數量）
	 * @return 組合後的路徑字串
	 */
	public static String assemblePath(String... segments) {
		return Arrays.stream(segments).filter(Objects::nonNull).map(s -> s.replaceAll("^/+", "").replaceAll("/+$", ""))
				.filter(s -> !s.isEmpty()).collect(Collectors.joining("/"));
	}

}
