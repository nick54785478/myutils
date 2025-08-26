package com.example.demo.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.util.ReflectionUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Map 轉換物件實體工具類
 * */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ParameterMappingUtil {

  private static final Map<Class<?>, Function<String, Object>> TYPE_CONVERTERS = new HashMap<>();

  /**
   * 日期模式清單
   */
  private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
      DateTimeFormatter.ofPattern("yyyy-MM-dd"),
      DateTimeFormatter.ofPattern("yyyy/MM/dd")
  );

  static {
    TYPE_CONVERTERS.put(Long.class, ParameterMappingUtil::parseLong);
    TYPE_CONVERTERS.put(long.class, ParameterMappingUtil::parseLong);

    TYPE_CONVERTERS.put(Integer.class, ParameterMappingUtil::parseInt);
    TYPE_CONVERTERS.put(int.class, ParameterMappingUtil::parseInt);

    TYPE_CONVERTERS.put(Double.class, Double::parseDouble);
    TYPE_CONVERTERS.put(double.class, Double::parseDouble);

    TYPE_CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
    TYPE_CONVERTERS.put(boolean.class, Boolean::parseBoolean);

    TYPE_CONVERTERS.put(BigDecimal.class, BigDecimal::new);
  }

  /**
   * 將 Map 的資料設置到實體中 (支援父類別屬性)
   */
  public static <T> T setFieldsFromMap(T entity, Map<String, String> fieldMap) {
    Class<?> entityClass = entity.getClass();

    for (Map.Entry<String, String> entry : fieldMap.entrySet()) {
      String fieldName = entry.getKey();
      String fieldValue = entry.getValue();
      try {
        Field field = getFieldRecursive(entityClass, fieldName);
        if (field == null) {
          log.warn("屬性 [{}] 在類別 [{}] 中不存在", fieldName, entityClass.getSimpleName());
          continue;
        }
        ReflectionUtils.makeAccessible(field);
        Object convertedValue = convertToFieldType(field, fieldValue);
        ReflectionUtils.setField(field, entity, convertedValue);

      } catch (Exception e) {
        log.error("設置屬性 [{}] 失敗，值: [{}]", fieldName, fieldValue, e);
      }
    }
    return entity;
  }

  /**
   * 遞迴查找 field (支援父類別)
   */
  private static Field getFieldRecursive(Class<?> clazz, String fieldName) {
    while (clazz != null) {
      try {
        return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException ignored) {
        clazz = clazz.getSuperclass();
      }
    }
    return null;
  }

  private static Object convertToFieldType(Field field, String value) {
    if (value == null) return null;

    Class<?> fieldType = field.getType();

    if (TYPE_CONVERTERS.containsKey(fieldType)) {
      return TYPE_CONVERTERS.get(fieldType).apply(value);
    }

    if (fieldType.isEnum()) {
      return Enum.valueOf((Class<Enum>) fieldType, value);
    }

    if (fieldType.equals(Date.class)) {
      return convertToDate(value, field.getName());
    }

    return value; // fallback: treat as String
  }

  private static Date convertToDate(String value, String fieldName) {
    // 1. 嘗試 LocalDate / LocalDateTime 格式
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        if (value.contains(":")) {
          return Date.from(LocalDateTime.parse(value, formatter).atZone(ZoneId.systemDefault()).toInstant());
        } else {
          return Date.from(LocalDate.parse(value, formatter).atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
      } catch (DateTimeParseException ignored) {
      }
    }

    // 2. 嘗試 Excel 數字日期
    try {
      double excelDate = Double.parseDouble(value);
      return convertExcelDateToDate(excelDate);
    } catch (NumberFormatException ignored) {
    }

    throw new IllegalArgumentException("Invalid date format for field: " + fieldName + " value: " + value);
  }

  private static Date convertExcelDateToDate(double excelDate) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(1900, Calendar.JANUARY, 1, 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    long days = (long) excelDate - 2; // Excel bug: 1900 被當成閏年
    calendar.add(Calendar.DATE, (int) days);

    double fractionalDay = excelDate - (long) excelDate;
    int millisecondsInDay = (int) (fractionalDay * 24 * 60 * 60 * 1000);
    calendar.add(Calendar.MILLISECOND, millisecondsInDay);

    return calendar.getTime();
  }

  private static Long parseLong(String v) {
    return Long.parseLong(trimExcelSuffix(v));
  }

  private static Integer parseInt(String v) {
    return Integer.parseInt(trimExcelSuffix(v));
  }

  private static String trimExcelSuffix(String v) {
    return v != null && v.endsWith(".0") ? v.substring(0, v.length() - 2) : v;
  }

  public static <T> List<String> getFields(T target) {
    return Arrays.stream(target.getClass().getDeclaredFields())
        .map(Field::getName)
        .toList();
  }
}

