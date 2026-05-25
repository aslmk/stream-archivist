package com.aslmk.storageservice.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AwsConfig {
    @Value("${minio.user}")
    private String minioUsername;
    @Value("${minio.password}")
    private String minioPassword;
    @Value("${minio.endpoint}")
    private String minioEndpoint;
    @Value("${minio.public-endpoint}")
    private String minioPublicEndpoint;

    private static final String SIGNING_REGION = "us-east-1";

    @Bean
    @Primary
    public AmazonS3 amazonS3Client() {
        return buildClient(minioEndpoint);
    }

    @Bean("presignS3Client")
    public AmazonS3 presignS3Client() {
        return buildClient(minioPublicEndpoint);
    }

    private AmazonS3 buildClient(String endpointUrl) {
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder
                .EndpointConfiguration(endpointUrl, SIGNING_REGION);

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(minioUsername, minioPassword)
                        )
                )
                .withPathStyleAccessEnabled(true)
                .build();
    }
}
