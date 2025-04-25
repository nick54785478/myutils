package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.model.employee.Employee;

@SpringBootTest
class JsonParseUtilTest {

	String jsonString = "";
	Employee employee = null;
	List<Employee> employeeList = new ArrayList<>();

	@BeforeEach
	void setUp() throws Exception {
		this.employee = new Employee(1L, "王小明", "Ming", 28, "ming@gmail.com");
		this.employeeList = List.of(new Employee(1L, "王小明", "Ming", 28, "mingWang@gmail.com"),
				new Employee(2L, "李大維", "David", 28, "davidLee@gmail.com"));
	}

	/**
	 * 測試: 1. Class 轉換為Json 2. Json 轉 Class
	 */
	@Test
	void testSerializeAndUnserialize() {
		// 物件轉Json
		this.jsonString = JsonParseUtil.serialize(this.employee);
		System.out.println("serialized result: " + this.jsonString);

		// Json 轉 物件
		Employee result = JsonParseUtil.unserialize(this.jsonString, Employee.class);
		System.out.println("unserialized result: " + result);
	}

	/**
	 * 測試: List 的轉換
	 */
	@Test
	void testUnserialize() {

		// Array 轉Json
		this.jsonString = JsonParseUtil.serialize(this.employeeList);
		System.out.println("serialized result: " + this.jsonString);

		// Json 轉 Array
		List<Employee> result = JsonParseUtil.unserializeArrayOfObject(this.jsonString, Employee.class);
		System.out.println("unserialized result:" + result);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
}
