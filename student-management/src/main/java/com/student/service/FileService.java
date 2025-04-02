package com.student.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件
     * @param file 文件
     * @param module 模块名称（如：homework, avatar等）
     * @return 文件访问URL
     */
    String uploadFile(MultipartFile file, String module);

    /**
     * 删除文件
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);

    /**
     * 下载文件
     * @param fileUrl 文件URL
     * @return 文件资源
     */
    Resource downloadFile(String fileUrl);

    /**
     * 获取文件的原始文件名
     * @param fileUrl 文件URL
     * @return 原始文件名
     */
    String getOriginalFileName(String fileUrl);
}
