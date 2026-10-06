package com.thedavelopers.eventqr.features.uploads.controller;

import java.io.ByteArrayInputStream;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thedavelopers.eventqr.features.uploads.model.dto.StoredFileResponse;
import com.thedavelopers.eventqr.features.uploads.service.FileStorageService;
import com.thedavelopers.eventqr.shared.constants.AccountRole;
import com.thedavelopers.eventqr.shared.exceptions.ForbiddenException;
import com.thedavelopers.eventqr.shared.response.ApiResponse;
import com.thedavelopers.eventqr.shared.security.JwtService;

@RestController
@RequestMapping("/api/v1")
public class UploadController {

    private static final Set<String> SHARED_PURPOSES =
            Set.of("event-poster", "event-logo", "id-template-asset", "id-template-logo");

    private final FileStorageService fileStorageService;
    private final JwtService jwtService;

    public UploadController(FileStorageService fileStorageService, JwtService jwtService) {
        this.fileStorageService = fileStorageService;
        this.jwtService = jwtService;
    }

    @PostMapping("/uploads/event-logo")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadEventLogo(HttpServletRequest request,
                                                                           @RequestParam("file") MultipartFile file) {
        UUID callerId = requireOrganizerRole(request);
        return ResponseEntity.ok(ApiResponse.success("Event poster stored", fileStorageService.store(callerId, "event-poster", file)));
    }

    @PostMapping("/uploads/id-template-assets")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadTemplateAsset(HttpServletRequest request,
                                                                               @RequestParam("file") MultipartFile file) {
        UUID callerId = requireOrganizerRole(request);
        return ResponseEntity.ok(ApiResponse.success("ID template asset stored", fileStorageService.store(callerId, "id-template-asset", file)));
    }

    @PostMapping("/uploads/profile-photo")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadProfilePhoto(HttpServletRequest request,
                                                                              @RequestParam("file") MultipartFile file) {
        UUID callerId = requireAuthenticated(request);
        return ResponseEntity.ok(ApiResponse.success("Profile photo stored", fileStorageService.store(callerId, "profile-photo", file)));
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<StoredFileResponse>> getFile(HttpServletRequest request, @PathVariable UUID fileId) {
        requireReadAccess(request, fileId);
        return ResponseEntity.ok(ApiResponse.success(fileStorageService.find(fileId)));
    }

    @GetMapping(value = "/files/{fileId}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFileContent(HttpServletRequest request, @PathVariable UUID fileId) {
        requireReadAccess(request, fileId);
        FileStorageService.StoredFileContent content = fileStorageService.readContent(fileId);
        MediaType mediaType = content.contentType() == null || content.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(content.contentType());
        // Streamed resource with explicit Content-Length: the bytea payload is bounded
        // at 5 MB by the upload path, and this avoids an extra full-body copy through the
        // JSON/base64 envelope. (Blob still crosses one memory hop until the S3 migration.)
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(content.content().length)
                .body(new InputStreamResource(new ByteArrayInputStream(content.content())));
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<StoredFileResponse>> deleteFile(HttpServletRequest request, @PathVariable UUID fileId) {
        requireOwnerOrAdmin(request, fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted", fileStorageService.delete(fileId)));
    }

    private UUID requireOrganizerRole(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        AccountRole role = jwtService.extractRoleFromBearer(authorization);
        if (role == AccountRole.ORGANIZER || role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return jwtService.extractUserIdFromBearer(authorization);
        }
        throw new ForbiddenException("Organizer or admin access required");
    }

    private UUID requireAuthenticated(HttpServletRequest request) {
        return jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
    }

    // Event posters and ID template images are shown to attendees and staff, so any
    // authenticated caller may read them. Everything else (profile photos, unknown
    // purposes, legacy owner-less files) is readable only by its owner or an admin.
    private void requireReadAccess(HttpServletRequest request, UUID fileId) {
        StoredFileResponse file = fileStorageService.find(fileId);
        if (SHARED_PURPOSES.contains(file.purpose())) {
            requireAuthenticated(request);
            return;
        }
        requireOwnerOrAdmin(request, file);
    }

    private void requireOwnerOrAdmin(HttpServletRequest request, UUID fileId) {
        requireOwnerOrAdmin(request, fileStorageService.find(fileId));
    }

    private void requireOwnerOrAdmin(HttpServletRequest request, StoredFileResponse file) {
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (file.ownerId() != null && callerId.equals(file.ownerId())) {
            return;
        }
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        throw new ForbiddenException("Access denied to file");
    }
}
