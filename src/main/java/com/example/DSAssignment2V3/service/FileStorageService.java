package com.example.DSAssignment2V3.service;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeFile(MultipartFile file);
    Resource loadFileAsResource(String fileName);
    void deleteFile(String fileName);
    String updateFile(String fileName, MultipartFile file);
    List<String> getAllFileNames();
    String readFileContent(String filename);
    void updateFileContent(String filename, String content);
}
