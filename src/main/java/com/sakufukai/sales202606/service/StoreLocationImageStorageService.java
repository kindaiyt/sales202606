package com.sakufukai.sales202606.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

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

    private final S3Client s3Client;

    public StoreLocationImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

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
        try {
            String key = s3Prefix + "/" + fileName;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(s3Bucket)
                            .key(key)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            String imageUrl = "https://" + s3Bucket + ".s3." + s3Region + ".amazonaws.com/" + key;

            return new StoreLocationImageResult(key, imageUrl);

        } catch (IOException e) {
            throw new IllegalArgumentException("S3へのアップロードに失敗しました。", e);
        }
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

    public void delete(String key) {
        if (key == null || key.isBlank()) {
            return;
        }

        if ("s3".equalsIgnoreCase(storageType)) {
            deleteFromS3(key);
        } else {
            deleteFromLocal(key);
        }
    }

    private void deleteFromS3(String key) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(key)
                        .build()
        );
    }

    private void deleteFromLocal(String key) {
        try {
            Path targetPath = Paths.get(localDir).resolve(key).normalize();

            Files.deleteIfExists(targetPath);

        } catch (IOException e) {
            throw new IllegalArgumentException("画像の削除に失敗しました。", e);
        }
    }

    public record StoreLocationImageResult(String key, String url) {
    }
}