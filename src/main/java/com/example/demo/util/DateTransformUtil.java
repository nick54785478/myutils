package com.example.demo.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 日期轉換工具
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTransformUtil {

	/**
	 * 將 LocalDateTime 轉換為 String
	 * 
	 * @param pattern       日期格式
	 * @param localDateTime 日期資料
	 * @return 日期字串
	 */
	public static String format(String pattern, LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}

		Date date = transformLocalDateTimeToDate(localDateTime);
		return format(pattern, date);
	}

	/**
	 * 將 LocalDate 轉為 String
	 * 
	 * @param pattern   日期格式
	 * @param localDate 日期資料
	 * @return 日期字串
	 */
	public static String format(String pattern, LocalDate localDate) {
		if (localDate == null) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return localDate.format(formatter);
	}

	/**
	 * 將 String 轉為 LocalDate
	 * 
	 * @param pattern   日期格式
	 * @param localDate 日期資料字串
	 * @return LocalDate 日期資料
	 */
	public static LocalDate transformStringToLocalDate(String pattern, String localDate) {
		if (localDate == null) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDate.parse(localDate, formatter);
	}

	/**
	 * String 轉換 Date
	 * 
	 * @param pattern 日期格式
	 * @param date    日期資料字串
	 * @return Date 日期資料
	 */
	public static Date parse(String pattern, String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		if (StringUtils.isBlank(date)) {
			return null;
		}

		return transformLocalDateTimeToDate(LocalDateTime.parse(date, formatter));
	}

	/**
	 * Date 轉換 String
	 * 
	 * @param pattern 日期格式
	 * @param date    日期資料
	 * @return 日期資料字串
	 */
	public static String format(String pattern, Date date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		if (Objects.isNull(date)) {
			return null;
		}
		LocalDateTime localDateTime = transformDateToLocalDateTime(date);
		return localDateTime.format(formatter);
	}

	/**
	 * 將 LocalDateTime 轉換為 Date
	 * 
	 * @param date 日期資料
	 * @return Date 日期資料
	 */
	private static Date transformLocalDateTimeToDate(LocalDateTime date) {
		if (Objects.isNull(date)) {
			return null;
		}
		return Date.from(date.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * 轉換字串為 LocalDateTime
	 * 
	 * @param pattern 日期格式
	 * @param date    日期資料字串
	 * @return LocalDateTime 日期資料
	 */
	public static LocalDateTime transformStringToLocalDateTime(String pattern, String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDateTime.parse(date, formatter);
	}

	/**
	 * 將 Date 轉換為 LocalDateTime
	 * 
	 * @param date    Date 日期資料
	 * @return LocalDateTime 日期資料
	 */
	private static LocalDateTime transformDateToLocalDateTime(Date date) {
		if (Objects.isNull(date)) {
			return null;
		}
		Instant instant = date.toInstant();
		return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * 根據 period 取得 第一天 (月、季、年)
	 */
	public static Date getFirstDayAccordingPeriod(String period) {
		if (StringUtils.equals(period, "YTD")) {
			return getFirstDayThisYear();
		} else if (StringUtils.equals(period, "QTD")) {
			return getFirstDayThisQuarter();
		} else {
			return getFirstDayThisMonth();
		}
	}

	/**
	 * 取得該季第一天
	 */
	private static Date getFirstDayThisQuarter() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 確定當前月份所處的季度
		int quarter = (calendar.get(Calendar.MONTH) / 3) + 1;
		// 設置日期為該季度的第一天
		int firstMonthOfQuarter = (quarter - 1) * 3;
		calendar.set(Calendar.MONTH, firstMonthOfQuarter); // Calendar.MONTH 從0開始（0代表一月，1代表二月，以此類推）。
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTime();
	}

	/**
	 * 取得當月第一天
	 */
	private static Date getFirstDayThisMonth() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		// 設置日期為當月的第一天
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		// 取得當月的第一天日期
		return calendar.getTime();
	}

	/**
	 * 取得當年第一天
	 */
	private static Date getFirstDayThisYear() {
		// 取得當前日期
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());

		// 設置日期為當年的第一天
		calendar.set(Calendar.MONTH, Calendar.JANUARY);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		// 取得當年的第一天日期
		return calendar.getTime();
	}

}
