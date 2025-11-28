package com.example.demo.util;

import java.math.BigDecimal;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 客製驗證工具類
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerValidateUtil {

	private static final ExpressionParser parser = new SpelExpressionParser();

	/**
	 * 客製驗證 將判斷值及條件 set 至 context 中，並取得 SpEL 語句進行判斷
	 * 
	 * @param checkedValue 檢查值
	 * @param operator     運算符號（例如 "=="、"!="、">"、"<="）
	 * @param expectValue  預期值
	 * @return 是否通過檢核
	 */
	public static boolean compare(BigDecimal checkedValue, String operator, BigDecimal expectValue) {
		String expression = generateExpression(operator);
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setVariable("checkedValue", checkedValue); // 檢查值
		context.setVariable("expectValue", expectValue); // 預期值
		return Boolean.TRUE.equals(parser.parseExpression(expression).getValue(context, Boolean.class));
	}

	/**
	 * 建立 Expression
	 * 
	 * @param operator   動作
	 * @param expression Expression
	 */
	private static String generateExpression(String operator) {
		return "#checkedValue " + operator + " #expectValue";
	}

}
