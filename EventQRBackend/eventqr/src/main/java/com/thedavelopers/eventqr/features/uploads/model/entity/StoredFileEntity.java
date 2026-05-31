package com.thedavelopers.eventqr.features.uploads.model.entity;

import java.time.Instant;
import java.util.UUID;

import com.thedavelopers.eventqr.shared.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stored_files")
public class StoredFileEntity extends BaseEntity {

    @Column(name = "owner_id")
    private UUID ownerId;

    private String purpose;

    private String fileName;

    private String contentType;

    private long size;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content = new byte[0];

    @Column(nullable = false)
    private Instant storedAt = Instant.now();
}
