package com.strider.user_profile.service;

import com.strider.strider_common_lib.error.StriderErrorCodes;
import com.strider.strider_common_lib.exception.StriderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    public String uploadImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("파일이 비어 있습니다.");
            }

            // 고유한 파일명 생성
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

            // 업로드 경로 (절대 경로 변환)
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);

            // 파일 저장
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 프론트에 반환할 URL (캐시 무효화를 위해 timestamp 추가)
            return baseUrl + "/static/uploads/" + filename + "?t=" + System.currentTimeMillis();

        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생", e);
            throw new StriderException(StriderErrorCodes.INTERNAL_SERVER_ERROR);
        }
    }
}