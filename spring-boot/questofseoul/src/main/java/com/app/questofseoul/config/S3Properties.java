package com.app.questofseoul.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

    private String bucket;
    private String region = "ap-northeast-2";
    private boolean enabled = false;
    private String accessKeyId;
    private String secretAccessKey;
}
