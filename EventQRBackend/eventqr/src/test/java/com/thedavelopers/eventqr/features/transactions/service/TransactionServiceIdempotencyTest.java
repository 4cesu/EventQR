package com.thedavelopers.eventqr.features.transactions.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionRequest;
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse;
import com.thedavelopers.eventqr.features.transactions.model.entity.TransactionLog;
import com.thedavelopers.eventqr.features.transactions.repository.TransactionLogRepository;
import com.thedavelopers.eventqr.features.transactions.repository.TransactionRuleRepository;
import com.thedavelopers.eventqr.shared.constants.TransactionResult;
import com.thedavelopers.eventqr.shared.constants.TransactionType;
import com.thedavelopers.eventqr.shared.exceptions.BadRequestException;
import com.thedavelopers.eventqr.shared.interfaces.AttendeeDirectoryPort;
import com.thedavelopers.eventqr.shared.interfaces.EventLookupPort;
import com.thedavelopers.eventqr.shared.interfaces.QrCredentialPort;
import com.thedavelopers.eventqr.shared.interfaces.RegistrationCommandPort;
import com.thedavelopers.eventqr.shared.interfaces.RegistrationLookupPort;
import com.thedavelopers.eventqr.shared.interfaces.ScanPurposePort;
import com.thedavelopers.eventqr.features.organizer.repository.EventStaffAssignmentRepository;

/**
 * A scan whose response was lost must not be logged twice when the staff member retries it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceIdempotencyTest {

    @Mock private TransactionLogRepository transactionLogRepository;
    @Mock private TransactionRuleRepository transactionRuleRepository;
    @Mock private EventLookupPort eventLookupPort;
    @Mock private ScanPurposePort scanPurposePort;
    @Mock private QrCredentialPort qrCredentialPort;
    @Mock private RegistrationLookupPort registrationLookupPort;
    @Mock private RegistrationCommandPort registrationCommandPort;
    @Mock private AttendeeDirectoryPort attendeeDirectoryPort;
    @Mock private EventStaffAssignmentRepository eventStaffAssignmentRepository;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    private TransactionService service;

    private final UUID eventId = UUID.randomUUID();
    private final UUID purposeId = UUID.randomUUID();
    private final UUID clientRequestId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = spy(new TransactionService(transactionLogRepository, transactionRuleRepository, eventLookupPort,
                scanPurposePort, qrCredentialPort, registrationLookupPort, registrationCommandPort,
                attendeeDirectoryPort, eventStaffAssignmentRepository, applicationEventPublisher, "Asia/Manila"));
    }

    private TransactionRequest request(UUID key) {
        return new TransactionRequest(eventId, purposeId, "qr-value", null, UUID.randomUUID(), null, key);
    }

    private TransactionLog logFor(UUID logEventId) {
        TransactionLog log = new TransactionLog();
        log.setId(UUID.randomUUID());
        log.setEventId(logEventId);
        log.setAttendeeUserId(UUID.randomUUID());
        log.setRegistrationId(UUID.randomUUID());
        log.setQrCredentialId(UUID.randomUUID());
        log.setScanPurposeId(purposeId);
        log.setTransactionType(TransactionType.ENTRY);
        log.setTransactionResult(TransactionResult.APPROVED);
        log.setScannedAt(Instant.now());
        return log;
    }

    private TransactionResponse responseFor(UUID transactionId) {
        return new TransactionResponse(transactionId, eventId, null, UUID.randomUUID(), null, UUID.randomUUID(), null,
                UUID.randomUUID(), purposeId, null, TransactionType.ENTRY, TransactionResult.APPROVED, 0, null,
                Instant.now());
    }

    @Test
    void retryWithSameKeyReturnsTheOriginalResultAndLogsNothingNew() {
        TransactionLog original = logFor(eventId);
        when(transactionLogRepository.findByClientRequestId(clientRequestId)).thenReturn(Optional.of(original));

        TransactionResponse response = service.record(request(clientRequestId));

        assertThat(response.transactionId()).isEqualTo(original.getId());
        verify(service, never()).recordNew(any());
        verify(transactionLogRepository, never()).save(any());
    }

    @Test
    void firstAttemptRecordsAndStoresTheKeyOnTheLog() {
        TransactionLog saved = logFor(eventId);
        when(transactionLogRepository.findByClientRequestId(clientRequestId)).thenReturn(Optional.empty());
        doReturn(responseFor(saved.getId())).when(service).recordNew(any());
        when(transactionLogRepository.findById(saved.getId())).thenReturn(Optional.of(saved));

        service.record(request(clientRequestId));

        assertThat(saved.getClientRequestId()).isEqualTo(clientRequestId);
    }

    @Test
    void keyAlreadyUsedForAnotherEventIsRejected() {
        when(transactionLogRepository.findByClientRequestId(clientRequestId))
                .thenReturn(Optional.of(logFor(UUID.randomUUID())));

        assertThatThrownBy(() -> service.record(request(clientRequestId)))
                .isInstanceOf(BadRequestException.class);
        verify(service, never()).recordNew(any());
    }

    @Test
    void requestWithoutKeyBehavesAsBefore() {
        doReturn(responseFor(UUID.randomUUID())).when(service).recordNew(any());

        service.record(request(null));

        verify(transactionLogRepository, never()).findByClientRequestId(any());
        verify(service).recordNew(any());
    }
}
