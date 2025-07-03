package com.example.demo.model.jxls;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerData {

	private String buyerTitle;

	private String buyerContact;

	private String buyerAddress;

	private String buyerTaxId;

	private String buyerPhone;

	private String buyerEmail;
}
