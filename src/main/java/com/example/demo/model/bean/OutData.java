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
public class OutData {

	private String id;
	
	private Long num;

	private String createDate;
	
	private Date updateDate;
	
	private BigDecimal bigDecimal1;
	
	private String bigDecimal2;

	private List<OuterData> dataList;

}
