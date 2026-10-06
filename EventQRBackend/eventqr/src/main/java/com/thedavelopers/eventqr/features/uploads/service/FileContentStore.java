package com.thedavelopers.eventqr.features.uploads.service;

/**
 * Where uploaded file bytes live when they are not in the database. Implemented for S3 and
 * S3-compatible services (Cloudflare R2, MinIO, DigitalOcean Spaces, ...).
 */
public interface FileContentStore {

    void put(String key, String contentType, byte[] content);

    /** @throws com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException if the object is missing */
    byte[] get(String key);

    void delete(String key);
}
