package com.student.service.impl;

import com.student.common.BusinessException;
import com.student.config.FileUploadConfig;
import com.student.service.FileService;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.student.common.Result.error;
import static com.student.common.Result.success;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    @Override
    public String uploadFile(MultipartFile file, String module) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException("Original filename cannot be null");
        }

        // 检查文件类型
        String extension = FilenameUtils.getExtension(originalFilename);
        if (!fileUploadConfig.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new BusinessException("File type not allowed");
        }

        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getMaxSize()) {
            throw new BusinessException("File size exceeds limit");
        }

        try {
            // 生成UUID并分段作为目录
            String uuid = UUID.randomUUID().toString();
            // 取UUID的前两段作为目录名
            String[] uuidParts = uuid.split("-");
            String uuidPath = String.format("%s/%s", uuidParts[0], uuidParts[1]);
            
            // 按日期和模块组织目录结构
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            
            // 完整的目录结构：baseDir/module/yyyy/MM/dd/uuid前两段/
            Path uploadDir = Paths.get(fileUploadConfig.getBaseDir(), module, datePath, uuidPath);
            Files.createDirectories(uploadDir);

            // 使用原始文件名
            Path filePath = uploadDir.resolve(originalFilename);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径作为URL（包含模块名）
            return Paths.get(module, datePath, uuidPath, originalFilename).toString();
        } catch (IOException e) {
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }

        try {
            Path filePath = Paths.get(fileUploadConfig.getBaseDir(), fileUrl);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public Resource downloadFile(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            throw new BusinessException("File URL cannot be empty");
        }

        try {
            Path filePath = Paths.get(fileUploadConfig.getBaseDir(), fileUrl);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("File not found or not readable");
            }
        } catch (MalformedURLException e) {
            throw new BusinessException("Invalid file URL");
        }
    }

    @Override
    public String getOriginalFileName(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return null;
        }

        // 直接返回路径中的最后一个部分，即原始文件名
        return Paths.get(fileUrl).getFileName().toString();
    }
}
