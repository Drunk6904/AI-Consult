package com.zhuofeng.ai_consult_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.zhuofeng.ai_consult_backend.service.DocumentParserService;
import com.zhuofeng.ai_consult_backend.service.DifyService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/knowledge")
public class KnowledgeController {

    private static final String UPLOAD_DIR = "uploads";
    private final DocumentParserService documentParserService;
    private final DifyService difyService;

    public KnowledgeController(DocumentParserService documentParserService, DifyService difyService) {
        this.documentParserService = documentParserService;
        this.difyService = difyService;
        // 创建上传目录
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                log.error("Failed to create upload directory", e);
            }
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // 获取文件名和扩展名
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName);

            // 验证文件类型
            if (!isAllowedFileType(fileExtension)) {
                response.put("success", false);
                response.put("message", "File type not allowed");
                return ResponseEntity.badRequest().body(response);
            }

            // 保存文件
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            file.transferTo(filePath);

            // 解析文件
            try {
                String content = documentParserService.parseDocument(filePath.toString());
                log.info("Document parsed successfully, content length: {}", content.length());

                // 分块处理
                List<String> chunks = documentParserService.chunkText(content, 1000);
                log.info("Text chunked successfully, chunk count: {}", chunks.size());

                response.put("contentLength", content.length());
                response.put("chunkCount", chunks.size());
            } catch (Exception e) {
                log.error("Failed to parse document", e);
                response.put("parseError", e.getMessage());
            }

            // 上传到Dify知识库
            try {
                log.info("Attempting to upload file to Dify: {}", fileName);
                String documentId = difyService.uploadDocument(filePath.toFile(), fileName).block();
                log.info("File uploaded to Dify successfully, document ID: {}", documentId);
                response.put("difyDocumentId", documentId);
                response.put("difyStatus", "success");
            } catch (Exception e) {
                log.error("Failed to upload to Dify", e);
                response.put("difyError", e.getMessage());
                response.put("difyStatus", "failed");
                // 即使Dify上传失败，也继续返回成功，因为文件已经成功保存到本地
                log.info("File saved locally even though Dify upload failed");
            }

            // 构建响应
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("fileName", fileName);
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Failed to upload file", e);
            response.put("success", false);
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isAllowedFileType(String extension) {
        // 允许的文件类型
        String[] allowedExtensions = { "pdf", "doc", "docx", "xls", "xlsx" };
        for (String allowedExtension : allowedExtensions) {
            if (allowedExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}
