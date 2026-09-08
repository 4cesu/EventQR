package com.thedavelopers.eventqr.features.users.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.thedavelopers.eventqr.features.users.model.entity.UserProfile;
import com.thedavelopers.eventqr.shared.constants.AccountRole;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByEmailIgnoreCase(String email);

    List<UserProfile> findTop20ByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(String email, String fullName);

    Page<UserProfile> findByRole(AccountRole role, Pageable pageable);

    Page<UserProfile> findByRoleNotIn(Collection<AccountRole> roles, Pageable pageable);
}
