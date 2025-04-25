package com.example.demo.util;

import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;

import com.example.demo.model.quotation.FormatQuoteData;
import com.example.demo.model.quotation.OrderFinancialSummaryData;
import com.example.demo.model.quotation.QuotationFormatResponse;
import com.example.demo.util.JasperReportUtil.ReportType;

@SpringBootTest
class JasperReportUtilTest {
	
	@Autowired
	ResourceLoader resourceLoader;

	ByteArrayResource resource = null;

	QuotationFormatResponse data = new QuotationFormatResponse();

	// 設置參數
	Map<String, Object> parameters = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		this.getMockData(); // 取得假資料
		this.getHeaders(); // 取得 Detail 表頭資料
		this.setParameterData(); // 設置 parameter 資料

	}

	@Test
	void test() throws SQLException {
		ResponseEntity<ByteArrayResource> responseEntity = JasperReportUtil.generateReportToDownload(ReportType.PDF,
				"Quotation", "Quotation.pdf", data.getFormatQuoteDataList(), parameters);
		this.resource = responseEntity.getBody();
		assertNotNull(this.resource);
	}

	@AfterEach
	void tearDown() throws Exception {
		// 本地端下載
		String outputPath = System.getProperty("user.dir") + "/src/main/resources" + "/result";
		// 從ByteArrayResource中讀取內容並寫入OutputStream
		try (FileOutputStream fos = new FileOutputStream(outputPath + "/report1.pdf")) {
			fos.write(resource.getContentAsByteArray());
		}

	}

	/**
	 * 取得假資料
	 * */
	private void getMockData() {
		data.setSellerTitle("WPG Holdings 大聯大控股股份有限公司");
		data.setSellerAddress("(115)南港區經貿二路189號21F");
		data.setSellerPhone("02 2191 0068");
		data.setSellerFax("02 2191 0068");
		data.setSellerEmail("hello@wpgholdings.com");
		data.setSellerContact("HappyWang 王開心");
		data.setBuyerTitle("ABC Co. 快樂公司");
		data.setBuyerAddress("台北市內湖區港墘路xxxxxx");
		data.setBuyerPhone("02 xxxx xxxx");
		data.setBuyerFax("02 xxxx xxxx");
		data.setBuyerEmail("buyer1@abc.com");
		data.setBuyerContact("Amy Chen 陳艾咪 / Buyer");
		data.setPaymentTerm("T/T");
		data.setShippingTerm(null);
		data.setCurrency("USD");
		this.getDetailData();
		this.getSubtotalAreaData();

	}

	/**
	 * 取得 Detail 資料
	 */
	private void getDetailData() {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		List<FormatQuoteData> formatQuoteDataList = new ArrayList<>();
		FormatQuoteData formatQuoteData = new FormatQuoteData();
		formatQuoteData.setQqNo("QQ231023000");
		formatQuoteData.setBrand("DIODE");
		formatQuoteData.setItemNo("BAV99-7-F");
		formatQuoteData.setErpVendorItemNo("BAV99-7-F");
		formatQuoteData.setQty(400L);
		formatQuoteData.setMoqSpq("100");
		formatQuoteData.setPackType("Tape and Re");
		formatQuoteData.setLt("10day");

		BigDecimal unitPrice = new BigDecimal(0.02).setScale(6, RoundingMode.HALF_UP);
		formatQuoteData.setUnitPrice(unitPrice.stripTrailingZeros());
		formatQuoteData.setAmount(new BigDecimal(8));

		String expirationDate = simpleDateFormat.format(new Date());
		formatQuoteData.setExpirationDate(expirationDate);
		formatQuoteDataList.add(formatQuoteData);
		data.setFormatQuoteDataList(formatQuoteDataList);
	}

	/**
	 * 取得 下方小計 資料
	 */
	private void getSubtotalAreaData() {
		// 下方小計
		OrderFinancialSummaryData orderFinancialSummaryData = new OrderFinancialSummaryData();
		orderFinancialSummaryData.setTotal("$ 10.5");
		orderFinancialSummaryData.setSalesTax("5.00 %");
		orderFinancialSummaryData.setSalesTaxAmount("$ 0.5");
		orderFinancialSummaryData.setSubtotal("$ 10");
		data.setOrderFinancialSummaryData(orderFinancialSummaryData);
	}

	/**
	 * 取得 Detail 表格資料
	 */
	private void getHeaders() {
		// 標題 (建議使用 P{}，彈性較大)
		parameters.put("erpVendorItemNoHeader", "Erp Vender Item No");
		parameters.put("qqNoHeader", "QQ#");
		parameters.put("brandHeader", "Brand");
		parameters.put("itemNoHeader", "Item No.");
		parameters.put("quotationHeader", "Quotation");
		parameters.put("moqSpqHeader", "MOQ/SP");
		parameters.put("packTypeHeader", "Pack Type");
		parameters.put("ltHeader", "L/T");
		parameters.put("unitPriceHeader", "Unit Price");
		parameters.put("amountHeader", "Amount($)");
		parameters.put("expirationDateHeader", "Expiration Date");
	}

	/**
	 * 設置 Parameter 資料
	 * @throws IOException 
	 */
	private void setParameterData() throws IOException {
		// 取得並設置 WPG Logo
		Resource logoResource = resourceLoader.getResource("classpath:/report/image/logo.png");
		parameters.put("logo", logoResource.getInputStream());
		
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
		parameters.put("nowDate", simpleDateFormat.format(new Date()));
		parameters.put("referenceNo", "00000001");
		// Seller
		parameters.put("sellerTitle", data.getSellerTitle());
		parameters.put("sellerAddress", data.getSellerAddress());
		parameters.put("sellerPhone", data.getSellerPhone());
		parameters.put("sellerFax", data.getSellerFax());
		parameters.put("sellerEmail", data.getSellerEmail());
		parameters.put("sellerContact", data.getSellerContact());
		// Buyer
		parameters.put("buyerTitle", data.getBuyerTitle());
		parameters.put("buyerAddress", data.getBuyerAddress());
		parameters.put("buyerPhone", data.getSellerPhone());
		parameters.put("buyerFax", data.getSellerFax());
		parameters.put("buyerEmail", data.getSellerEmail());

		// 左中表格
		parameters.put("buyerContact", data.getBuyerContact());
		parameters.put("paymentTerm", data.getPaymentTerm());
		parameters.put("shipTerm", data.getShippingTerm());
		parameters.put("currency", data.getCurrency());
		
		// 下方小計
		OrderFinancialSummaryData orderFinancialSummaryData = data.getOrderFinancialSummaryData();
		parameters.put("total", orderFinancialSummaryData.getTotal());
		parameters.put("subtotal", orderFinancialSummaryData.getSubtotal());
		parameters.put("salesTax", orderFinancialSummaryData.getSalesTax());
		parameters.put("salesTaxAmount", orderFinancialSummaryData.getSalesTaxAmount());
		parameters.put("note", "1. Quote Valid for 14 days");

	}

}
