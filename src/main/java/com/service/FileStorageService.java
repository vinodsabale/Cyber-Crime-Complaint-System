package com.service;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.entity.Complaint;
import com.entity.Evidence;
import com.exception.FileStorageException;

import lombok.extern.slf4j.Slf4j;
 
@Service
@Slf4j
public class FileStorageService {
 
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;
 
    @Value("${app.upload.allowed-types:pdf,doc,docx,jpg,jpeg,png,txt}")
    private String allowedTypes;
 
    private static final long MAX_SIZE = 10 * 1024 * 1024;
 
    public Evidence store(MultipartFile file, Complaint complaint, String uploadedBy) {
        validate(file);
        String ext    = FilenameUtils.getExtension(file.getOriginalFilename());
        String stored = UUID.randomUUID() + "." + ext;
        String date   = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path   dir    = Paths.get(uploadDir, String.valueOf(complaint.getId()), date);
 
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(stored);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
 
            return Evidence.builder()
                    .complaint(complaint)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(stored)
                    .filePath(target.toString())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .evidenceType(detectType(ext))
                    .uploadedBy(uploadedBy)
                    .verified(false)
                    .build();
 
        } catch (IOException e) {
            throw new FileStorageException("Failed to store: " + file.getOriginalFilename(), e);
        }
    }
 
    public Resource load(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) return resource;
            throw new FileStorageException("Cannot read file: " + filePath);
        } catch (MalformedURLException e) {
            throw new FileStorageException("Bad path: " + filePath, e);
        }
    }
 
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("Could not delete: {}", filePath);
        }
    }
 
    private void validate(MultipartFile file) {
        if (file.isEmpty()) throw new FileStorageException("Empty file not allowed");
        if (file.getSize() > MAX_SIZE) throw new FileStorageException("File exceeds 10MB limit");
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        if (!allowed.contains(ext.toLowerCase()))
            throw new FileStorageException("File type not allowed: ." + ext);
    }
 
    private Evidence.EvidenceType detectType(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg", "png" -> Evidence.EvidenceType.SCREENSHOT;
            case "pdf"                -> Evidence.EvidenceType.DOCUMENT;
            case "mp4", "avi"         -> Evidence.EvidenceType.VIDEO;
            default                   -> Evidence.EvidenceType.OTHER;
        };
    }
}
 