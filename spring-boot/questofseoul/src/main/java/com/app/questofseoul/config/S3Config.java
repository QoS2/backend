package com.app.questofseoul.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.s3.enabled", havingValue = "true")
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider = createCredentialsProvider();
        return S3Client.builder()
            .region(Region.of(s3Properties.getRegion()))
            .credentialsProvider(credentialsProvider)
            .build();
    }

    private AwsCredentialsProvider createCredentialsProvider() {
        String accessKeyId = s3Properties.getAccessKeyId();
        String secretAccessKey = s3Properties.getSecretAccessKey();
        if (accessKeyId != null && !accessKeyId.isBlank()
            && secretAccessKey != null && !secretAccessKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        }
        return DefaultCredentialsProvider.create();
    }
}
