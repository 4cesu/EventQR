package com.thedavelopers.eventqr.features.events.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.thedavelopers.eventqr.features.events.model.dto.AttendeeEventResponse;
import com.thedavelopers.eventqr.features.events.model.dto.EventApprovalRequest;
import com.thedavelopers.eventqr.features.events.model.dto.EventAvailabilityResponse;
import com.thedavelopers.eventqr.features.events.model.dto.EventRequest;
import com.thedavelopers.eventqr.features.events.model.dto.EventResponse;
import com.thedavelopers.eventqr.features.events.model.entity.Event;
import com.thedavelopers.eventqr.features.events.repository.EventRepository;
import com.thedavelopers.eventqr.shared.constants.EventStatus;
import com.thedavelopers.eventqr.shared.exceptions.ConflictException;
import com.thedavelopers.eventqr.shared.exceptions.ResourceNotFoundException;

class EventServiceTest {

    private EventRepository repository;
    private EventService service;
    private final UUID organizerId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(EventRepository.class);
        service = new EventService(repository);
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Event event(EventStatus status, Instant opensAt, Instant closesAt, Integer capacity, Integer attending) {
        Event e = new Event();
        e.setId(eventId);
        e.setTitle("Tech Conf");
        e.setStatus(status);
        e.setRegistrationOpenAt(opensAt);
        e.setRegistrationCloseAt(closesAt);
        e.setCapacity(capacity);
        e.setCurrentAttendeeCount(attending);
        e.setOrganizerUserId(organizerId);
        when(repository.findById(eventId)).thenReturn(Optional.of(e));
        return e;
    }

    private Instant ago(long seconds) {
        return Instant.now().minusSeconds(seconds);
    }

    private Instant ahead(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }

    // ----- create -----

    @Test
    void aNewEventStartsPendingReviewWithNoAttendeesAndBelongsToTheCaller() {
        EventRequest request = new EventRequest("  Tech Conf  ", "desc", "Tech", "Hall A", null, ahead(60), ahead(3_600),
                ahead(7_200), ahead(10_800), 100, true);

        EventResponse response = service.create(organizerId, request);

        assertThat(response.status()).isEqualTo(EventStatus.PENDING_REVIEW);
        assertThat(response.title()).isEqualTo("Tech Conf");
        assertThat(response.currentAttendeeCount()).isZero();
        assertThat(response.organizerUserId()).isEqualTo(organizerId);
        assertThat(response.rewardsEnabled()).isTrue();
        assertThat(response.capacity()).isEqualTo(100);
    }

    @Test
    void rewardsAreOffUnlessExplicitlyEnabled() {
        EventRequest request = new EventRequest("Tech Conf", null, null, null, null, null, null, null, null, 0, null);

        assertThat(service.create(organizerId, request).rewardsEnabled()).isFalse();
    }

    // ----- review -----

    @Test
    void approvingRecordsTheReviewerAndClearsAnyRejectionReason() {
        Event e = event(EventStatus.PENDING_REVIEW, null, null, 10, 0);
        e.setRejectionReason("old reason");
        UUID reviewer = UUID.randomUUID();

        EventResponse response = service.review(eventId, new EventApprovalRequest(true, reviewer, "ignored"));

        assertThat(response.status()).isEqualTo(EventStatus.APPROVED);
        assertThat(response.approvedByUserId()).isEqualTo(reviewer);
        assertThat(response.approvedAt()).isNotNull();
        assertThat(response.rejectionReason()).isNull();
    }

    @Test
    void rejectingStoresTheReasonAndNoApprovalTime() {
        event(EventStatus.PENDING_REVIEW, null, null, 10, 0);

        EventResponse response = service.review(eventId,
                new EventApprovalRequest(false, UUID.randomUUID(), "Venue unavailable"));

        assertThat(response.status()).isEqualTo(EventStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Venue unavailable");
        assertThat(response.approvedAt()).isNull();
    }

    @Test
    void reviewingAnUnknownEventIsNotFound() {
        when(repository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.review(eventId, new EventApprovalRequest(true, UUID.randomUUID(), null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ----- activate / status -----

    @Test
    void onlyAnApprovedEventCanBeActivated() {
        event(EventStatus.APPROVED, null, null, 10, 0);
        assertThat(service.activate(eventId).status()).isEqualTo(EventStatus.ACTIVE);

        for (EventStatus status : List.of(EventStatus.DRAFT, EventStatus.PENDING_REVIEW, EventStatus.REJECTED,
                EventStatus.ACTIVE, EventStatus.ENDED, EventStatus.CANCELLED)) {
            event(status, null, null, 10, 0);
            assertThatThrownBy(() -> service.activate(eventId))
                    .as("from %s", status)
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Test
    void updatingTheStatusSetsItDirectly() {
        event(EventStatus.ACTIVE, null, null, 10, 0);

        assertThat(service.updateStatus(eventId, EventStatus.CANCELLED).status()).isEqualTo(EventStatus.CANCELLED);
    }

    @Test
    void activatingAnUnknownEventIsNotFound() {
        when(repository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(eventId)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ----- availability: the registration rules the app shows -----

    @Test
    void anOpenEventWithRoomCanAcceptRegistrations() {
        event(EventStatus.APPROVED, ago(60), ahead(60), 10, 3);

        EventAvailabilityResponse a = service.availability(eventId);

        assertThat(a.available()).isTrue();
        assertThat(a.registrationOpen()).isTrue();
        assertThat(a.full()).isFalse();
        assertThat(a.message()).isEqualTo("Event can accept registrations");
    }

    @Test
    void aFullEventIsNotAvailableAndSaysSo() {
        event(EventStatus.ACTIVE, ago(60), ahead(60), 10, 10);

        EventAvailabilityResponse a = service.availability(eventId);

        assertThat(a.full()).isTrue();
        assertThat(a.available()).isFalse();
        assertThat(a.message()).isEqualTo("Event is at capacity");
    }

    @Test
    void capacityZeroMeansUnlimited() {
        event(EventStatus.ACTIVE, ago(60), ahead(60), 0, 5_000);

        assertThat(service.availability(eventId).full()).isFalse();
        assertThat(service.availability(eventId).available()).isTrue();
    }

    @Test
    void registrationNotYetOpenAndAlreadyClosedHaveDistinctMessages() {
        event(EventStatus.APPROVED, ahead(3_600), ahead(7_200), 10, 0);
        assertThat(service.availability(eventId).message()).isEqualTo("Registration not open yet");

        event(EventStatus.APPROVED, ago(7_200), ago(60), 10, 0);
        assertThat(service.availability(eventId).message()).isEqualTo("Registration is closed");
    }

    @Test
    void anEventThatIsNotPublicIsNotOpenForRegistration() {
        for (EventStatus status : List.of(EventStatus.DRAFT, EventStatus.PENDING_REVIEW, EventStatus.REJECTED,
                EventStatus.ENDED, EventStatus.CANCELLED)) {
            event(status, ago(60), ahead(60), 10, 0);

            EventAvailabilityResponse a = service.availability(eventId);

            assertThat(a.registrationOpen()).as("%s", status).isFalse();
            assertThat(a.message()).isEqualTo("Event is not open for registration");
        }
    }

    @Test
    void missingCountsAreTreatedAsZero() {
        event(EventStatus.APPROVED, null, null, null, null);

        EventAvailabilityResponse a = service.availability(eventId);

        assertThat(a.capacity()).isZero();
        assertThat(a.currentAttendeeCount()).isZero();
        assertThat(a.available()).isTrue();
    }

    // ----- attendee views -----

    @Test
    void theAttendeeViewFlagsAnEventTheCallerOwns() {
        event(EventStatus.APPROVED, null, null, 10, 0);

        AttendeeEventResponse mine = service.findAttendeeEvent(eventId, organizerId);
        AttendeeEventResponse someoneElses = service.findAttendeeEvent(eventId, UUID.randomUUID());

        assertThat(mine.isOwnedByCurrentUser()).isTrue();
        assertThat(someoneElses.isOwnedByCurrentUser()).isFalse();
    }

    @Test
    void publicListingsOnlyAskForApprovedAndActiveEvents() {
        when(repository.findByStatusIn(any(), any())).thenReturn(new PageImpl<>(List.of()));

        service.findAllEvents(PageRequest.of(0, 20));

        verify(repository).findByStatusIn(List.of(EventStatus.APPROVED, EventStatus.ACTIVE), PageRequest.of(0, 20));
    }

    // ----- attendee counters -----

    @Test
    void incrementingAFullEventIsAConflict() {
        when(repository.incrementAttendeeCountIfAvailable(eventId)).thenReturn(0);

        assertThatThrownBy(() -> service.incrementCurrentAttendeeCount(eventId)).isInstanceOf(ConflictException.class);
    }

    @Test
    void incrementingAnEventWithRoomSucceeds() {
        when(repository.incrementAttendeeCountIfAvailable(eventId)).thenReturn(1);

        service.incrementCurrentAttendeeCount(eventId);

        verify(repository).incrementAttendeeCountIfAvailable(eventId);
    }

    @Test
    void decrementingDelegatesToTheClampedDatabaseUpdate() {
        service.decrementCurrentAttendeeCount(eventId);

        verify(repository).decrementAttendeeCount(eventId);
    }
}
