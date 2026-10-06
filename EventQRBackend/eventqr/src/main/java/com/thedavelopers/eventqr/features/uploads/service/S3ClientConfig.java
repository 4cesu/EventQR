package com.thedavelopers.eventqr.features.uploads.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * Builds the S3 client only when {@code app.storage.type=s3}. Works with AWS S3 and with
 * S3-compatible services by setting an endpoint (and usually path-style access).
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(@Value("${app.storage.s3.region:us-east-1}") String region,
                             @Value("${app.storage.s3.endpoint:}") String endpoint,
                             @Value("${app.storage.s3.access-key:}") String accessKey,
                             @Value("${app.storage.s3.secret-key:}") String secretKey,
                             @Value("${app.storage.s3.path-style:false}") boolean pathStyle) {
        AwsCredentialsProvider credentials = (accessKey.isBlank() || secretKey.isBlank())
                ? DefaultCredentialsProvider.create()
                : StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyle).build())
                // Only add checksums when S3 requires them: several S3-compatible services reject
                // the default CRC checksum headers newer SDKs send.
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED);
        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
