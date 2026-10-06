package com.thedavelopers.eventqr.features.attendance.controller;

import com.thedavelopers.eventqr.features.registrations.model.dto.RegistrationResponse;
import com.thedavelopers.eventqr.features.transactions.model.dto.TransactionResponse;
import com.thedavelopers.eventqr.shared.constants.TransactionType;
import com.thedavelopers.eventqr.shared.constants.TransactionResult;
import com.thedavelopers.eventqr.shared.constants.RegistrationStatus;
import com.thedavelopers.eventqr.features.registrations.service.RegistrationService;
import com.thedavelopers.eventqr.features.transactions.service.TransactionService;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for AttendeeController.
 */
@ExtendWith(MockitoExtension.class)
class AttendeeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegistrationService registrationService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private JwtService jwtService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        // Standalone setup: a @WebMvcTest slice would boot the full security filter
        // chain and its unmocked dependencies, which this controller test doesn't need.
        mockMvc = MockMvcBuilders.standaloneSetup(
                new AttendeeController(registrationService, transactionService, jwtService)).build();
    }

    @Test
    void testMyTransactions_Success() throws Exception {
        // Arrange
        String fakeToken = "Bearer fake-jwt-token";
TransactionResponse mockTransaction = new TransactionResponse(
                UUID.randomUUID(), // transactionId
                UUID.randomUUID(), // eventId
                "Tech Conference 2026", // eventTitle
                testUserId, // attendeeUserId
                "Jane Doe", // attendeeName
                UUID.randomUUID(), // registrationId
                RegistrationStatus.REGISTERED.name(), // registrationStatus
                UUID.randomUUID(), // qrCredentialId
                UUID.randomUUID(), // scanPurposeId
                "Ticket Scan", // scanPurposeName
                TransactionType.REGISTRATION, // transactionType
                TransactionResult.APPROVED, // transactionResult
                10, // pointsDelta
                "Ticket purchase", // reason
                Instant.now() // scannedAt
        );
        given(jwtService.extractUserIdFromBearer(fakeToken)).willReturn(testUserId);
        given(transactionService.findByAttendee(testUserId)).willReturn(List.of(mockTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendees/me/transactions")
                        .header("Authorization", fakeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].eventTitle").value("Tech Conference 2026"))
                .andExpect(jsonPath("$.data[0].transactionType").value("REGISTRATION"))
                .andExpect(jsonPath("$.data[0].pointsDelta").value(10))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testMyEventStatus_Success() throws Exception {
        // Arrange
        String fakeToken = "Bearer fake-jwt-token";
        UUID eventId = UUID.randomUUID();
        RegistrationResponse mockRegistration = new RegistrationResponse(
                UUID.randomUUID(), // registrationId
                eventId, // eventId
                testUserId, // attendeeUserId
                "attendee@eventqr.com", // attendeeEmail
                "Jane Doe", // attendeeName
                com.thedavelopers.eventqr.shared.constants.RegistrationStatus.REGISTERED, // status
                UUID.randomUUID(), // qrCredentialId
                Instant.now(), // registeredAt
                "Tech Conference 2026", // eventTitle
                "Conference Center", // eventLocation
                Instant.now().plusSeconds(86400), // eventStartAt
                Instant.now().plusSeconds(172800), // eventEndAt
                "+1233334444", // attendeePhoneNumber
                null, // enteredAt
                null, // exitedAt
                null, // attendedAt
                100, // pointsEarned
                12345, // registrationNumber
                "ATTENDEE" // attendeeRole
        );
        given(jwtService.extractUserIdFromBearer(fakeToken)).willReturn(testUserId);
        given(registrationService.findByAttendeeUserId(testUserId)).willReturn(List.of(mockRegistration));

        // Act & Assert
        mockMvc.perform(get("/api/v1/attendees/me/events/{eventId}/status", eventId)
                        .header("Authorization", fakeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventTitle").value("Tech Conference 2026"))
                .andExpect(jsonPath("$.data.attendeeName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));
    }
}