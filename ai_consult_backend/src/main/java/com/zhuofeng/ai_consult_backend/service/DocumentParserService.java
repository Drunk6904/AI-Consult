package com.zhuofeng.ai_consult_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DocumentParserService {

    /**
     * 解析文档文件，提取文本内容
     * @param filePath 文件路径
     * @return 提取的文本内容
     */
    public String parseDocument(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        String fileName = file.getName();
        String extension = getFileExtension(fileName);

        switch (extension) {
            case "pdf":
                return parsePDF(file);
            case "doc":
            case "docx":
                return parseWord(file);
            case "xls":
            case "xlsx":
                return parseExcel(file);
            default:
                throw new IOException("Unsupported file type: " + extension);
        }
    }

    /**
     * 解析PDF文件
     */
    private String parsePDF(File file) throws IOException {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析Word文件
     */
    private String parseWord(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        }
    }

    /**
     * 解析Excel文件
     */
    private String parseExcel(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;
            if (file.getName().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else {
                workbook = new HSSFWorkbook(fis);
            }

            StringBuilder text = new StringBuilder();
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                text.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                            Cell cell = row.getCell(cellIndex);
                            if (cell != null) {
                                text.append(getCellValue(cell)).append("\t");
                            } else {
                                text.append("\t");
                            }
                        }
                        text.append("\n");
                    }
                }
                text.append("\n");
            }
            workbook.close();
            return text.toString();
        }
    }

    /**
     * 获取Excel单元格值
     */
    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 解析文档并进行文本分块
     * @param filePath 文件路径
     * @param chunkSize 分块大小
     * @return 文本块列表
     */
    public List<String> parseAndChunkDocument(String filePath, int chunkSize) throws IOException {
        String content = parseDocument(filePath);
        return chunkText(content, chunkSize);
    }

    /**
     * 文本分块逻辑
     * @param text 文本内容
     * @param chunkSize 分块大小
     * @return 文本块列表
     */
    public List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // 按段落分割
        String[] paragraphs = text.split("\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() + 1 <= chunkSize) {
                // 当前块可以容纳该段落
                if (currentChunk.length() > 0) {
                    currentChunk.append("\n");
                }
                currentChunk.append(paragraph);
            } else {
                // 当前块已满，添加到列表并开始新块
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                }

                // 处理长段落
                if (paragraph.length() > chunkSize) {
                    // 按句子分割长段落
                    String[] sentences = paragraph.split("[。！？.!?]");
                    StringBuilder sentenceChunk = new StringBuilder();
                    for (String sentence : sentences) {
                        if (sentenceChunk.length() + sentence.length() + 1 <= chunkSize) {
                            if (sentenceChunk.length() > 0) {
                                sentenceChunk.append("。");
                            }
                            sentenceChunk.append(sentence);
                        } else {
                            if (sentenceChunk.length() > 0) {
                                chunks.add(sentenceChunk.toString());
                                sentenceChunk = new StringBuilder();
                            }
                            sentenceChunk.append(sentence);
                        }
                    }
                    if (sentenceChunk.length() > 0) {
                        chunks.add(sentenceChunk.toString());
                    }
                } else {
                    currentChunk.append(paragraph);
                }
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }
}
