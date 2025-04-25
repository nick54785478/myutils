package com.example.demo.model.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

	private Long id;

	private String name;
	
	private String engName;
	
	private Integer age;
	
	private String email;

}
