package com.example.demo.model.quotation;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationFormatResponse {
    private String title;
    private String referenceNo;
    private String date;
    private String sellerTitle;
    private String sellerContact;
    private String sellerAddress;
    private String sellerPhone;

    private String sellerFax;
    private String sellerEmail;
    private String buyerTitle;
    private String buyerContact;
    private String buyerAddress;
    private String buyerUuid;
    private String buyerPhone;
    private String buyerFax;
    private String buyerEmail;
    private String paymentTerm;
    private String shippingTerm;
    private String currency;
    private String expirationDate;
    private List<FormatQuoteData> formatQuoteDataList;
    private OrderFinancialSummaryData orderFinancialSummaryData;
    private String note;
}
