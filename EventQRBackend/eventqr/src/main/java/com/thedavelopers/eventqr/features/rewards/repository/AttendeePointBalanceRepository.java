package com.thedavelopers.eventqr.features.rewards.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thedavelopers.eventqr.features.rewards.model.entity.AttendeePointBalance;

public interface AttendeePointBalanceRepository extends JpaRepository<AttendeePointBalance, UUID> {

    Optional<AttendeePointBalance> findByEventIdAndAttendeeUserId(UUID eventId, UUID attendeeUserId);

    @Query("select coalesce(sum(b.pointsBalance), 0) from AttendeePointBalance b where b.attendeeUserId = :attendeeUserId")
    long sumPointsByAttendeeUserId(@Param("attendeeUserId") UUID attendeeUserId);

    Page<AttendeePointBalance> findByAttendeeUserId(UUID attendeeUserId, Pageable pageable);

    Page<AttendeePointBalance> findByEventId(UUID eventId, Pageable pageable);

    /**
     * Atomic point balance delta — requires unique index (event_id, attendee_user_id).
     * Returns 1 if updated, 0 if no matching row found.
     * (Design §5.3)
     */
    @Modifying
    @Query("UPDATE AttendeePointBalance b SET b.pointsBalance = b.pointsBalance + :delta, b.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE b.eventId = :eventId AND b.attendeeUserId = :attendeeUserId")
    int addPointsDelta(@Param("eventId") UUID eventId,
                       @Param("attendeeUserId") UUID attendeeUserId,
                       @Param("delta") int delta);

    /**
     * Atomic upsert: insert-or-add. Uses DB ON CONFLICT for the unique constraint.
     * (Design §5.4)
     */
    @Modifying
    @Query(value = "INSERT INTO attendee_point_balances (id, event_id, attendee_user_id, points_balance, created_at, updated_at) " +
                   "VALUES (gen_random_uuid(), :eventId, :attendeeUserId, :points, now(), now()) " +
                   "ON CONFLICT (event_id, attendee_user_id) DO UPDATE " +
                   "SET points_balance = attendee_point_balances.points_balance + EXCLUDED.points_balance, " +
                   "updated_at = now()",
           nativeQuery = true)
    int upsertPoints(@Param("eventId") UUID eventId,
                     @Param("attendeeUserId") UUID attendeeUserId,
                     @Param("points") int points);
}
