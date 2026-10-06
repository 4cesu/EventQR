package com.thedavelopers.eventqr.features.uploads.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.thedavelopers.eventqr.features.uploads.model.dto.StoredFileResponse;
import com.thedavelopers.eventqr.features.uploads.service.FileStorageService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.GlobalExceptionHandler;
import com.thedavelopers.eventqr.shared.security.JwtService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UploadControllerTest {

    private static final String OWNER_TOKEN = "Bearer owner";
    private static final String OTHER_TOKEN = "Bearer other";
    private static final String ADMIN_TOKEN = "Bearer admin";

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID otherId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UploadController(fileStorageService, jwtService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        authenticate(OWNER_TOKEN, ownerId, AccountRole.ATTENDEE);
        authenticate(OTHER_TOKEN, otherId, AccountRole.ORGANIZER);
        authenticate(ADMIN_TOKEN, adminId, AccountRole.ADMIN);
    }

    @Test
    void profilePhotoUpload_isStoredWithCallerAsOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "me.png", "image/png", new byte[] {1, 2, 3, 4});

        mockMvc.perform(multipart("/api/v1/uploads/profile-photo").file(file).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isOk());

        verify(fileStorageService).store(eq(ownerId), eq("profile-photo"), any());
    }

    @Test
    void eventPosterUpload_isStoredWithCallerAsOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "poster.png", "image/png", new byte[] {1, 2, 3, 4});

        mockMvc.perform(multipart("/api/v1/uploads/event-logo").file(file).header("Authorization", OTHER_TOKEN))
                .andExpect(status().isOk());

        verify(fileStorageService).store(eq(otherId), eq("event-poster"), any());
    }

    @Test
    void profilePhoto_otherUserCannotRead() throws Exception {
        storedFile(ownerId, "profile-photo");

        mockMvc.perform(get("/api/v1/files/{id}", fileId).header("Authorization", OTHER_TOKEN))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/files/{id}/content", fileId).header("Authorization", OTHER_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void profilePhoto_ownerAndAdminCanRead() throws Exception {
        storedFile(ownerId, "profile-photo");

        mockMvc.perform(get("/api/v1/files/{id}", fileId).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/files/{id}", fileId).header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void ownerlessProfilePhoto_isNotReadableByOrdinaryUsers() throws Exception {
        storedFile(null, "profile-photo");

        mockMvc.perform(get("/api/v1/files/{id}", fileId).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void eventPoster_isReadableByAnyAuthenticatedUser() throws Exception {
        storedFile(otherId, "event-poster");

        mockMvc.perform(get("/api/v1/files/{id}", fileId).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void eventPoster_otherUserCannotDelete() throws Exception {
        storedFile(otherId, "event-poster");

        mockMvc.perform(delete("/api/v1/files/{id}", fileId).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isForbidden());

        verify(fileStorageService, never()).delete(any());
    }

    @Test
    void ownerlessFile_onlyAdminCanDelete() throws Exception {
        storedFile(null, "event-poster");

        mockMvc.perform(delete("/api/v1/files/{id}", fileId).header("Authorization", OTHER_TOKEN))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/files/{id}", fileId).header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk());

        verify(fileStorageService).delete(fileId);
    }

    @Test
    void owner_canDeleteOwnFile() throws Exception {
        storedFile(ownerId, "profile-photo");

        mockMvc.perform(delete("/api/v1/files/{id}", fileId).header("Authorization", OWNER_TOKEN))
                .andExpect(status().isOk());

        verify(fileStorageService).delete(fileId);
    }

    private void authenticate(String token, UUID userId, AccountRole role) {
        when(jwtService.extractUserIdFromBearer(token)).thenReturn(userId);
        when(jwtService.extractRoleFromBearer(token)).thenReturn(role);
    }

    private void storedFile(UUID owner, String purpose) {
        StoredFileResponse response = new StoredFileResponse(fileId, owner, purpose, "f.png", "image/png", 4,
                "AVAILABLE", Instant.now(), "");
        when(fileStorageService.find(fileId)).thenReturn(response);
        when(fileStorageService.delete(fileId)).thenReturn(response);
        when(fileStorageService.readContent(fileId))
                .thenReturn(new FileStorageService.StoredFileContent(new byte[] {1, 2, 3, 4}, "image/png"));
    }
}
