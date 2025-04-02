package com.student.controller;

import com.student.common.BusinessException;
import com.student.common.Result;
import com.student.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 文件上传
     * @param file 文件
     * @param module 模块
     * @return 文件访问URL
     */
    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("module") String module) {
        try {
            String fileUrl = fileService.uploadFile(file, module);
            return Result.success(fileUrl);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public Result<?> deleteFile(@RequestParam String fileUrl) {
        try {
            fileService.deleteFile(fileUrl);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下载文件
     * @param fileUrl 文件URL
     * @param response HTTP响应
     */
    @GetMapping("/download")
    @PreAuthorize("permitAll()")
    public void downloadFile(@RequestParam String fileUrl, HttpServletResponse response) {
        try {
            Resource resource = fileService.downloadFile(fileUrl);
            String originalFilename = fileService.getOriginalFileName(fileUrl);
            
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + URLEncoder.encode(originalFilename, "UTF-8") + "\"");
            
            // 将文件内容写入响应流
            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            throw new BusinessException("Failed to download file: " + e.getMessage());
        }
    }
}
