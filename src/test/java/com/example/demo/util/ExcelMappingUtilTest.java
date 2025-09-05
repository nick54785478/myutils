package com.example.demo.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import groovy.transform.ToString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

class ExcelMappingUtilTest {

	@Data
	@ToString
	@NoArgsConstructor
	@AllArgsConstructor
	class Person {
		private Long number;
		private String name;
		private Integer age;
		private Date birthday;
	}
	

	@Test
	void testSetFieldsFromMap() throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
		String birthday = "1995/09/01";
		Map<String, String> map = Map.of("number", "1", "name", "Nick", "age", "29", "birthday", "1995/09/01");
		Person result = ExcelMappingUtil.setFieldsFromMap(new Person(), map);
		System.out.println(result);
		assertEquals(result.getNumber(), 1);
		assertEquals(result.getName(), "Nick");
		assertEquals(result.getAge(), 29);
		assertEquals(result.getBirthday(), simpleDateFormat.parse(birthday));

	}

}
