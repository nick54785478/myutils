package com.example.demo.spel;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContextRoot {

	private Map<String, String> currentRow; // 當前處理的 row
	
	private String currentCellValue;	// 當前驗證值
	
	private List<Map<String, String>> sheet; // Sheet 資料( Excel 轉換的 List<Map>)

	/**
	 * 透過 FieldName 取得該 FieldValue
	 * 
	 * @param fieldName
	 * @return String
	 */
	public String getFieldValue(String fieldName) {
		return currentRow.get(fieldName);
	}

	public String getCurrentFieldValue() {
		return currentRow.values().stream().findFirst().orElse("");
	}

}
