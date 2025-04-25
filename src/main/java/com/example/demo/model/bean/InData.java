package com.example.demo.model.bean;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InData {
	
	private Long id;
	
	private String num;

	private Date createDate;
	
	private String updateDate;
	
	private String bigDecimal1;
	
	private BigDecimal bigDecimal2;

	private List<InnerData> dataList;
}
