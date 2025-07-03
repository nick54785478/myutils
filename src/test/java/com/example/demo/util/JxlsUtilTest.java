package com.example.demo.util;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import com.example.demo.model.jxls.BuyerData;
import com.example.demo.model.jxls.UserProfileV;

@SpringBootTest
class JxlsUtilTest {

	@Autowired
	ResourceLoader resourceLoader;
	static final String RESOURCE = System.getProperty("user.dir") + "/src/main/resources"; // 靜態資源路徑

	BuyerData data = new BuyerData();

	Map<String, Object> map = new HashMap<>();

	@BeforeEach
	void setUp() throws Exception {
		// UserProfile
		List<UserProfileV> userProfileVs = List
				.of(new UserProfileV("王開心", "Happy Wang", "hello@wpgholdings.com", "02 2191 0068"));
		map.put("userProfileVs", userProfileVs);

		// 取得 Buyer 資料
		this.getMockData();
		map.put("buyerDatas", List.of(data));

	}

	/**
	 * 測試單表
	 */
	@Test
	void testExportExcel() throws IOException {
		var resource = resourceLoader.getResource("classpath:/jxls/quotation.xlsx");
		try (OutputStream outputStream = new FileOutputStream(new File(RESOURCE + "/jxls/result/quotation.xlsx"))) {
			JxlsUtil.exportExcel(resource.getInputStream(), outputStream, map);
			assertNotNull(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error");
		}

	}

	/**
	 * 測試多表
	 */
	@Test
	void testExportMultipleSheetExcel() {
		var resource = resourceLoader.getResource("classpath:/jxls/multi_quotation.xlsx");
		try (OutputStream outputStream = new FileOutputStream(
				new File(RESOURCE + "/jxls/result/multi_quotation.xlsx"))) {

			// 註. 為方便測試 sheet1 與 sheet2 內容其實是相同的，實務上應該會不同
			Map<String, Map<String, Object>> model = new HashMap<>();
			model.put("sheet1", map);
			model.put("sheet2", map);
			JxlsUtil.exportMultiSheetExcel(resource.getInputStream(), outputStream, model);
			assertNotNull(outputStream);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error");
		}
	}

	/**
	 * 取得假資料
	 */
	private void getMockData() {
		data.setBuyerTitle("精誠資訊股份有限公司");
		data.setBuyerAddress("台北市內湖區瑞光路 318 號");
		data.setBuyerPhone("02 7720 1888");
		data.setBuyerTaxId("97311466");
		data.setBuyerEmail("buyer1@abc.com");
		data.setBuyerContact("Amy Chen 陳艾咪 / Buyer");
	}

}
