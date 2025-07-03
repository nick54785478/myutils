package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.demo.spec.ExcelFieldValidator;

class CustomerValidateUtilTest {

	private List<ExcelFieldValidator> validators = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {

	}

	@Test
	void testCompare() {
		assertTrue(CustomerValidateUtil.compare(new BigDecimal("5"), "==", new BigDecimal("5")));
		assertFalse(CustomerValidateUtil.compare(new BigDecimal("5"), "==", new BigDecimal("6")));
		assertTrue(CustomerValidateUtil.compare(new BigDecimal("10"), ">", new BigDecimal("5")));

		assertFalse(CustomerValidateUtil.compare(new BigDecimal("5"), ">", new BigDecimal("10")));

		assertTrue(CustomerValidateUtil.compare(new BigDecimal("8"), ">=", new BigDecimal("8")));
		assertFalse(CustomerValidateUtil.compare(new BigDecimal("3"), ">=", new BigDecimal("5")));

		assertTrue(CustomerValidateUtil.compare(new BigDecimal("3"), "<", new BigDecimal("10")));
		assertFalse(CustomerValidateUtil.compare(new BigDecimal("10"), "<", new BigDecimal("3")));

		assertTrue(CustomerValidateUtil.compare(new BigDecimal("6"), "!=", new BigDecimal("7")));
		assertFalse(CustomerValidateUtil.compare(new BigDecimal("6"), "!=", new BigDecimal("6")));
	}

//	@Test
	void testNotEqual() {

//		// 設定 Excel 欄位檢核規則
//		this.validators = List.of(new ExcelFieldValidator("金額", "#value != null && #value > 0"), // 金額需大於0
//				new ExcelFieldValidator("姓名", "#value != null && #value.length() >= 2"), // 姓名至少2個字
//				new ExcelFieldValidator("年齡", "#value != null && #value >= 18"), // 年齡需滿 18 歲
//				new ExcelFieldValidator("Email", "#value != null && #value.matches('.+@.+\\..+')") // Email 格式檢查
//		);
//
//		// 測試不同欄位數據
//		Object[][] testData = { { "金額", new BigDecimal("100") }, { "金額", new BigDecimal("-10") }, { "姓名", "阿明" },
//				{ "姓名", "小" }, { "年齡", 20 }, { "年齡", 16 }, { "Email", "test@example.com" },
//				{ "Email", "invalid-email" } };
//
//		for (Object[] data : testData) {
//
//			String fieldName = (String) data[0];
//			Object fieldValue = data[1];
//
//			validators.stream().filter(v -> v.getFieldName().equals(fieldName)).findFirst().ifPresent(v -> {
//				boolean valid = v.validate(fieldValue);
//				System.out.printf("檢查 %s = %s，結果: %s%n", fieldName, fieldValue, valid ? "✅ 合格" : "❌ 不合格");
//			});
//		}

	}

}
