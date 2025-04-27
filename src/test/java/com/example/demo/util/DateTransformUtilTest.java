package com.example.demo.util;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DateTransformUtilTest {

	@Test
	void testParse() {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		String date = "2024-04-29 00:00:00";
		Date parsedDate = DateTransformUtil.parse(pattern, date);
		System.out.println("parsed date: "+ parsedDate);
		assertNotNull(parsedDate);
	
	}
	
	
	@Test
	void testFormat() {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		Date now = new Date();
		String formattedDate = DateTransformUtil.format(pattern, now);
		System.out.println("formatted date: "+ formattedDate);
		assertNotNull(formattedDate);

	}
	
	@Test
	void testGetFirstDayAccordingPeriod() {
		String period = "QTD";
		Date firstDay = DateTransformUtil.getFirstDayAccordingPeriod(period);
		System.out.println("First Day of "+ period + ": " +firstDay);
		assertNotNull(firstDay);
	}

}
