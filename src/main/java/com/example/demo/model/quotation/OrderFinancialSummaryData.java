package com.example.demo.model.quotation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFinancialSummaryData {
    private String subtotal;
    private String salesTax;
    private String salesTaxAmount;
    private String total;


}
