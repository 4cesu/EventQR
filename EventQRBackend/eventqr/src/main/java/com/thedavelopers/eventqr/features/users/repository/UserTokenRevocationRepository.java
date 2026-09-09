package com.thedavelopers.eventqr.features.users.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.users.model.entity.UserTokenRevocation;

public interface UserTokenRevocationRepository extends JpaRepository<UserTokenRevocation, UUID> {

    Optional<UserTokenRevocation> findByUserId(UUID userId);
}