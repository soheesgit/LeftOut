package com.example.demo.TEST_001.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.directory:src/main/resources/static/uploads/recipes}")
    private String uploadDir;

    @Value("${file.upload.url-prefix:/uploads/recipes}")
    private String urlPrefix;

    // 최대 파일 크기: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 허용된 MIME 타입
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif"
    );

    // 파일 업로드 처리
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)");
        }

        // 파일 확장자 검증
        String originalFilename = sanitizeFilename(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        validateFileExtension(extension);

        // 업로드 디렉토리 생성
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            boolean created = uploadDirectory.mkdirs();
            if (!created) {
                log.error("업로드 디렉토리 생성 실패: {}", uploadDir);
                throw new IOException("업로드 디렉토리를 생성할 수 없습니다.");
            }
        }

        // 고유한 파일명 생성 (UUID 사용)
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(uploadDir, uniqueFilename);

        // 파일 저장 (try-with-resources 사용)
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 업로드 성공: {}", uniqueFilename);
        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생: {}", uniqueFilename, e);
            throw e;
        }

        // 웹에서 접근 가능한 URL 경로 반환
        return urlPrefix + "/" + uniqueFilename;
    }

    // 파일명 sanitization
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("파일명이 null입니다.");
        }
        // 경로 조작 방지 및 특수문자 제거
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    // 파일 삭제
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // URL 경로를 실제 파일 경로로 변환
            String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path path = Paths.get(uploadDir, filename);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.info("파일 삭제 성공: {}", filename);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filename);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    // 파일 확장자 검증
    private void validateFileExtension(String extension) {
        String[] allowedExtensions = {".jpg", ".jpeg", ".png", ".gif"};
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)");
    }
}
