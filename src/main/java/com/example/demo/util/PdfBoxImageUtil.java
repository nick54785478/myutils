package com.example.demo.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.core.io.InputStreamResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * PDF 圖片抽取工具
 * <p>
 * 使用 PDFBox 從 PDF 文件中抽取圖片，支援：
 * <ul>
 *     <li>抽取第一張圖片並返回 InputStreamResource</li>
 *     <li>抽取第一張圖片並返回 Base64 字串</li>
 *     <li>抽取所有圖片並壓縮成 Zip，返回 InputStreamResource 或 Base64</li>
 * </ul>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PdfBoxImageUtil {

    /**
     * 從 PDF 中抽取第一張圖片並轉為 InputStreamResource。
     *
     * @param pdfBytes PDF 內容
     * @return InputStreamResource 包含圖片，若無圖片則返回 null
     * @throws IOException PDF 讀取或轉換失敗
     */
    public static InputStreamResource extractFirstImage(byte[] pdfBytes) throws IOException {
        return extractFirstImageInternal(pdfBytes, false);
    }

    /**
     * 從 PDF 中抽取第一張圖片並轉為 Base64 字串。
     *
     * @param pdfBytes PDF 內容
     * @return Base64 字串，若無圖片則返回 null
     * @throws IOException PDF 讀取或轉換失敗
     */
    public static String extractFirstImageAsBase64(byte[] pdfBytes) throws IOException {
        InputStreamResource resource = extractFirstImageInternal(pdfBytes, true);
        if (resource == null) return null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            resource.getInputStream().transferTo(baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 從 PDF 中抽取所有圖片並壓縮成 Zip，返回 InputStreamResource。
     *
     * @param pdfBytes PDF 內容
     * @return InputStreamResource (Zip 格式)，若無圖片則 Zip 內為空
     * @throws IOException PDF 讀取或壓縮失敗
     */
    public static InputStreamResource extractAllImagesAsZip(byte[] pdfBytes) throws IOException {
        List<Pair<byte[], String>> images = extractAllImages(pdfBytes);
        return zipImages(images);
    }

    /**
     * 從 PDF 中抽取所有圖片並壓縮成 Zip，返回 Base64 字串。
     *
     * @param pdfBytes PDF 內容
     * @return Base64 字串 (Zip 格式)，若無圖片則返回空字串
     * @throws IOException PDF 讀取或壓縮失敗
     */
    public static String extractAllImagesAsZipBase64(byte[] pdfBytes) throws IOException {
        List<Pair<byte[], String>> images = extractAllImages(pdfBytes);
        InputStreamResource zipResource = zipImages(images);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            zipResource.getInputStream().transferTo(baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    /**
     * 抽取 PDF 中的所有圖片。
     *
     * @param pdfBytes PDF 內容
     * @return List<Pair<byte[], format>> 每張圖片的 byte[] 與格式
     * @throws IOException PDF 讀取失敗
     */
    private static List<Pair<byte[], String>> extractAllImages(byte[] pdfBytes) throws IOException {
        List<Pair<byte[], String>> imageList = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            int pageIndex = 0;
            for (PDPage page : document.getPages()) {
                pageIndex++;
                PDResources resources = page.getResources();

                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject image) {
                        String format = Optional.ofNullable(image.getSuffix()).filter(s -> !s.isBlank()).orElse("png");
                        BufferedImage bufferedImage = image.getImage();

                        ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, format, imgBaos);

                        imageList.add(Pair.of(imgBaos.toByteArray(), format));
                        log.info("Extracted image from page {} with format {}", pageIndex, format);
                    }
                }
            }
        }

        return imageList;
    }

    /**
     * 將圖片列表壓縮成 Zip，返回 InputStreamResource。
     *
     * @param images List<Pair<byte[], format>> 每張圖片的 byte[] 與格式
     * @return InputStreamResource (Zip 格式)
     * @throws IOException 壓縮失敗
     */
    private static InputStreamResource zipImages(List<Pair<byte[], String>> images) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            int index = 1;
            for (Pair<byte[], String> imgPair : images) {
                String entryName = "image_" + index++ + "." + imgPair.getRight();
                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(imgPair.getLeft());
                zos.closeEntry();
            }
        }

        return new InputStreamResource(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * 抽取 PDF 中的第一張圖片，根據需求返回 InputStreamResource 或 Base64 包裝。
     *
     * @param pdfBytes PDF 內容
     * @param asResource 是否返回 InputStreamResource (false: 只返回 null)
     * @return InputStreamResource 或 null
     * @throws IOException PDF 讀取失敗
     */
    private static InputStreamResource extractFirstImageInternal(byte[] pdfBytes, boolean asResource) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                for (COSName name : resources.getXObjectNames()) {
                    PDXObject xobject = resources.getXObject(name);
                    if (xobject instanceof PDImageXObject image) {
                        String format = Optional.ofNullable(image.getSuffix()).filter(s -> !s.isBlank()).orElse("png");
                        BufferedImage bImage = image.getImage();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bImage, format, baos);
                        return new InputStreamResource(new ByteArrayInputStream(baos.toByteArray()));
                    }
                }
            }
        }
        return null; // 沒有圖片
    }
}
