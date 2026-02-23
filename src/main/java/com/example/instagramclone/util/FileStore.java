package com.example.instagramclone.util;

import com.example.instagramclone.exception.PostErrorCode;
import com.example.instagramclone.exception.PostException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class FileStore {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Value("${file.upload.location}")
    private String fileDir;

    // 💡 놀랍게도 스프링이 콤마를 인식해서 Set으로 변환해 줍니다!
    @Value("${file.upload.allowed-extensions}")
    private Set<String> allowedExtensions;

    @PostConstruct
    public void init() {
        File dir = new File(fileDir);
        if (!dir.exists()) {
            dir.mkdirs(); // 디렉토리가 없으면 생성
        }
    }

    public String storeFile(MultipartFile multipartFile) throws IOException {

        // 1. 원본 파일명 추출
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new PostException(PostErrorCode.INVALID_FILE_EXTENSION);
        }

        // 확장자 및 MIME 타입 검증
        String contentType = multipartFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new PostException(PostErrorCode.INVALID_FILE_EXTENSION);
        }

        String lowerOriginal = originalFilename.toLowerCase();
        if (allowedExtensions.stream().noneMatch(lowerOriginal::endsWith)) {
            throw new PostException(PostErrorCode.INVALID_FILE_EXTENSION);
        }

        // 2. 서버에 저장할 고유 파일명 생성 (UUID 활용)
        int extIndex = originalFilename.lastIndexOf(".");
        String ext = (extIndex == -1) ? "" : originalFilename.substring(extIndex);
        String storeFileName = UUID.randomUUID().toString() + ext;

        // 3. 전체 파일 저장 경로 문자열 만들기 (fileDir + 고유파일명)
        String fullPath = fileDir + storeFileName;

        // 4. 로컬 디스크에 파일 저장 (multipartFile.transferTo() 활용)
        multipartFile.transferTo(new File(fullPath));

        // 5. 클라이언트가 접근할 수 있는 정적 리소스 경로 구조로 반환 (WebMvcConfig 활용)
        return "/img/" + storeFileName;
    }
}
