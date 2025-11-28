package com.example.demo.util;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DroolsUtil {

	private static final KieContainer KIE_CONTAINER;
	private static SpreadsheetCompiler converter = new SpreadsheetCompiler();

	static {
		try {
			KieServices kieServices = KieServices.Factory.get();
			KIE_CONTAINER = kieServices.getKieClasspathContainer();
		} catch (Exception e) {
			throw new RuntimeException("初始化 Drools 失敗", e);
		}
	}

	/**
	 * 執行業務
	 * 
	 * @param sessionName kmodule.xml 中定義的 ksession 名稱
	 * @param target      目標物件
	 */
	public static void execute(String sessionName, Object target) {
		KieSession kieSession = generateKieSession(sessionName);
		try {
			kieSession.insert(target);
			kieSession.fireAllRules(); // 紀錄觸發數量
		} catch (Exception e) {
			log.error("發生錯誤", e);
		} finally {
			kieSession.dispose();
		}
	}

	/**
	 * 執行業務邏輯(清單的驗證)
	 * 
	 * @param sessionName kmodule.xml 中定義的 ksession 名稱
	 * @param targetList  目標物件清單
	 */
	public static void execute(String sessionName, List<Object> targetList) {
		KieSession kieSession = generateKieSession(sessionName);
		try {
			targetList.stream().forEach(target -> {
				kieSession.insert(target);
			});
			kieSession.fireAllRules(); // 紀錄觸發數量

		} catch (Exception e) {
			log.error("發生錯誤", e);
		} finally {
			kieSession.dispose();
		}
	}

	/**
	 * 透過 Decision Table 執行業務邏輯的驗證
	 * 
	 * @param is     InputStream
	 * @param target 目標物件
	 */
	public static void executeByDecisionTable(InputStream is, Object target) {
		KieSession kieSession = generateKieSession(is);
		kieSession.insert(target);
		kieSession.fireAllRules(); // 紀錄觸發數量
	}

	/**
	 * 透過 Decision Table 執行業務邏輯的驗證
	 * 
	 * @param is         InputStream
	 * @param targetList 目標物件清單
	 */
	public static void executeByDecisionTable(InputStream is, List<Object> targetList) {
		KieSession kieSession = generateKieSession(is);
		targetList.stream().forEach(target -> {
			kieSession.insert(target);
		});
		kieSession.fireAllRules(); // 紀錄觸發數量

	}

	/**
	 * 取得指定名稱的 KieSession
	 * 
	 * @param sessionName kmodule.xml 中定義的 ksession 名稱
	 * @return KieSession 實例
	 */
	public static KieSession generateKieSession(String sessionName) {
		try {
			return KIE_CONTAINER.newKieSession(sessionName);
		} catch (Exception e) {
			throw new RuntimeException("無法建立 KieSession: " + sessionName, e);
		}
	}

	/**
	 * 轉換 Decision Table 為指定的 KieSession
	 * 
	 * @param is InputStream
	 * @return KieSession 實例
	 */
	public static KieSession generateKieSession(InputStream is) {
		String drl = transformExcelToDrl(is);
		KieHelper kieHelper = new KieHelper();
		kieHelper.addContent(drl, ResourceType.DRL); // 加入 DRL 字串
		return kieHelper.build().newKieSession(); // 產生 session
	}

	/**
	 * 將決策表轉換為 drl
	 * 
	 * @param is InputStream
	 * @return drl
	 */
	private static String transformExcelToDrl(InputStream is) {
		if (Objects.isNull(is)) {
			return "";
		}
		// 將決策表轉換為 DRL，
		// 註. XLSX 會被轉換為 XLS
		return converter.compile(is, InputType.XLS);
	}

}
