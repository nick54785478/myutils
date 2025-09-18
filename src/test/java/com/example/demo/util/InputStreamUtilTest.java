package com.example.demo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class InputStreamUtilTest {

    private File tempFile; // 測試過程建立的暫存檔

    @AfterEach
    void cleanup() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void testToByteArrayAndFromByteArray() throws Exception {
        String text = "Hello, InputStream!";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        byte[] bytes = InputStreamUtil.toByteArray(inputStream);
        assertEquals(text, new String(bytes, StandardCharsets.UTF_8));

        InputStream restored = InputStreamUtil.fromByteArray(bytes);
        assertEquals(text, InputStreamUtil.toString(restored));
    }

    @Test
    void testToString() throws Exception {
        String text = "測試 UTF-8 字串";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        String result = InputStreamUtil.toString(inputStream);
        assertEquals(text, result);
    }

    @Test
    void testToFile() throws Exception {
        String text = "File writing test";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        tempFile = File.createTempFile("inputstream_test_", ".txt");
        String path = tempFile.getAbsolutePath();

        File writtenFile = InputStreamUtil.toFile(inputStream, path);

        assertTrue(writtenFile.exists());
        assertEquals(text, new String(java.nio.file.Files.readAllBytes(writtenFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    void testToBase64() throws Exception {
        String text = "Base64 test";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        String base64 = InputStreamUtil.toBase64(inputStream);
        String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);

        assertEquals(text, decoded);
    }

    @Test
    void testCloneInMemory() throws Exception {
        String text = "Clone test";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));

        InputStream[] clones = InputStreamUtil.clone(inputStream, 2);

        for (InputStream clone : clones) {
            assertEquals(text, InputStreamUtil.toString(clone));
        }
    }

    @Test
    void testCloneLarge() throws Exception {
        // 建立大檔案內容 (模擬 5MB)
        byte[] largeData = new byte[5 * 1024 * 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 127);
        }
        InputStream inputStream = new ByteArrayInputStream(largeData);

        InputStream[] clones = InputStreamUtil.cloneLarge(inputStream, 2);

        for (InputStream clone : clones) {
            byte[] readBytes = InputStreamUtil.toByteArray(clone);
            assertArrayEquals(largeData, readBytes);
            clone.close();
        }
    }

    @Test
    void testBufferedImageConversion() throws Exception {
        // 建立一張 100x100 紅色圖片
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        // BufferedImage -> InputStream
        InputStream inputStream = InputStreamUtil.bufferedImageToInputStream(image, "png");
        assertNotNull(inputStream);

        // InputStream -> BufferedImage
        BufferedImage restored = InputStreamUtil.toBufferedImage(inputStream);
        assertNotNull(restored);
        assertEquals(100, restored.getWidth());
        assertEquals(100, restored.getHeight());
    }
}
