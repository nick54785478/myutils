package com.example.demo.spec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateErrorProperty {

	private String rule;
	
	private int rowIndex;
	
	private String message;
}
