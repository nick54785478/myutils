package com.example.demo.spec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationPolicy {
	
	private String type; // 驗證類型，ROW、SHEET

	private String rule; // 規則，如: ENFORCE_ROW_VALIDATION -> 表示即使欄位為空，仍要執行驗證邏輯

	private String mappingFieldName; // Mapping Field Name

	private String expression; // SpEL Expression
}
