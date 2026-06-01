package com.thedavelopers.eventqr.features.uploads.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.uploads.model.entity.StoredFileEntity;

public interface StoredFileRepository extends JpaRepository<StoredFileEntity, UUID> {
}
