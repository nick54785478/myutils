package com.example.demo.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.example.demo.model.drool.Person;

class DroolsUtilTest {

	  /**
	   * 測試從 kmodule.xml 載入規則
	   */
	  @Test
	  public void testExecuteFromKmodule() {
	    Person p1 = new Person("Alice", 20);
	    Person p2 = new Person("Bob", 15);

	    // 單物件
	    DroolsUtil.execute("ksession-rules", p1);

	    // 多物件
	    DroolsUtil.execute("ksession-rules", Arrays.asList(p1, p2));

//	    System.out.println(p1);
//	    System.out.println(p2);
	  }

	  /**
	   * 測試從 Decision Table Excel 載入規則
	   */
//	  @Test
	  public void testExecuteFromDecisionTable() throws Exception {
	    byte[] fileBytes = Files.readAllBytes(Paths.get("src/main/resources/rules/isXls.xlsx"));
	    Person p1 = new Person("張三", 30);
	    Person p2 = new Person("李四", 18);
	    try (InputStream is1 = new ByteArrayInputStream(fileBytes)) {
	      DroolsUtil.executeByDecisionTable(is1, p1);
	    }
	    try (InputStream is2 = new ByteArrayInputStream(fileBytes)) {
	      DroolsUtil.executeByDecisionTable(is2, p2);
	    }
	    System.out.println(p1);
	    System.out.println(p1.isResult());
	    System.out.println(p2);
	    System.out.println(p2.isResult());
	  }
	}