package com.example.demo.model.quotation;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormatQuoteData {
    private String qqNo;
    private String brand;
    private String itemNo;
    private String erpVendorItemNo;
    private Long qty;
    private String moqSpq;
    private String packType;
    private String lt;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String expirationDate;
    private String confirmYes;
    private String confirmNo;
}
