package com.example.demo.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FreeMarkerTemplateGenerateUtilTest {

	  @Test
	  void renderTemplate() {
	    Map<String, Object> model = Map.of(
	      "recipientName", "John Doe",
	      "formNo", "PSB52025120141",
	      "url", "https://sqm.example.com/8d/PSB52025120141"
	    );
	    String html = FreeMarkerTemplateGenerateUtil.renderTemplate( "notify-template.html",
	      FreeMarkerTemplateGenerateUtilTest.class.getResourceAsStream("/freemarker/notify-template.html"),
	      model
	    );

	    System.out.println(html);
	  }

	  @Test
	  void renderTemplateWithEmptyBody() throws IOException {
	    Map<String, Object> model = Map.of(
	      "recipientName", "John Doe",
	      "formNo", "PSB52025120141",
	      "url", "https://sqm.example.com/8d/PSB52025120141"
	    );
	    String body = new String(FreeMarkerTemplateGenerateUtilTest.class.getResourceAsStream("/freemarker/notify-template.html")
	      .readAllBytes(), StandardCharsets.UTF_8);

	    String content = FreeMarkerTemplateGenerateUtil.renderTemplateFromString("notify-template.html", body,  model);
	    System.out.println("content: " + content);
	  }
	}
