package com.thedavelopers.eventqr.features.uploads.model.entity;

import java.time.Instant;
import java.util.UUID;

import com.thedavelopers.eventqr.shared.utils.BaseEntity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stored_files")
public class StoredFile extends BaseEntity {

    private UUID ownerId;

    private String purpose;

    private String fileName;

    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private Instant storedAt;

    /** Object key when the bytes live in S3-compatible storage; null while they are in the database. */
    @Column(name = "storage_key", length = 512)
    private String storageKey;

    /** The bytes for database-stored files; null once a file lives in object storage. */
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "bytea")
    private byte[] content;
}
