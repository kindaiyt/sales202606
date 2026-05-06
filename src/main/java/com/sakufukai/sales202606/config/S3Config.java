package com.sakufukai.sales202606.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(@Value("${app.image-storage.s3-region}") String s3Region) {
        return S3Client.builder()
                .region(Region.of(s3Region))
                .build();
    }
}