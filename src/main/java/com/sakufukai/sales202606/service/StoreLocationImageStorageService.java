package com.sakufukai.sales202606.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class StoreLocationImageStorageService {

    @Value("${app.image-storage.type}")
    private String storageType;

    @Value("${app.image-storage.local-dir}")
    private String localDir;

    @Value("${app.image-storage.public-path}")
    private String publicPath;

    @Value("${app.image-storage.s3-bucket}")
    private String s3Bucket;

    @Value("${app.image-storage.s3-prefix}")
    private String s3Prefix;

    @Value("${app.image-storage.s3-region}")
    private String s3Region;

    public StoreLocationImageResult save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = buildFileName(file);

        if ("s3".equalsIgnoreCase(storageType)) {
            return saveToS3(file, fileName);
        }

        return saveToLocal(file, fileName);
    }

    private StoreLocationImageResult saveToLocal(MultipartFile file, String fileName) {
        try {
            Path dir = Paths.get(localDir);
            Files.createDirectories(dir);

            Path savePath = dir.resolve(fileName);
            Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = publicPath + "/" + fileName;

            return new StoreLocationImageResult(fileName, imageUrl);

        } catch (IOException e) {
            throw new IllegalArgumentException("画像の保存に失敗しました。", e);
        }
    }

    private StoreLocationImageResult saveToS3(MultipartFile file, String fileName) {
        // 後でAWS SDK実装に差し替える場所
        // 先に設計だけ通すなら、ここはまだ例外でOK
        throw new UnsupportedOperationException("S3保存処理は未実装です。");

        /*
        String key = s3Prefix + "/" + fileName;

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(key)
                .contentType(file.getContentType())
                .build(),
            RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        String imageUrl = "https://" + s3Bucket + ".s3." + s3Region + ".amazonaws.com/" + key;

        return new StoreLocationImageResult(key, imageUrl);
        */
    }

    private String buildFileName(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        if (!extension.equals(".png")
                && !extension.equals(".jpg")
                && !extension.equals(".jpeg")
                && !extension.equals(".webp")) {
            throw new IllegalArgumentException("画像は png, jpg, jpeg, webp のみアップロードできます。");
        }

        return UUID.randomUUID() + extension;
    }

    public record StoreLocationImageResult(String key, String url) {
    }
}