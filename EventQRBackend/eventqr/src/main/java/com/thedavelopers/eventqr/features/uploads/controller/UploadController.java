package com.thedavelopers.eventqr.features.uploads.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
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

    private final FileStorageService fileStorageService;
    private final JwtService jwtService;

    public UploadController(FileStorageService fileStorageService, JwtService jwtService) {
        this.fileStorageService = fileStorageService;
        this.jwtService = jwtService;
    }

    @PostMapping("/uploads/event-logo")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadEventLogo(HttpServletRequest request,
                                                                           @RequestParam("file") MultipartFile file) {
        requireOrganizerRole(request);
        return ResponseEntity.ok(ApiResponse.success("Event poster stored", fileStorageService.store(null, "event-poster", file)));
    }

    @PostMapping("/uploads/id-template-assets")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadTemplateAsset(HttpServletRequest request,
                                                                               @RequestParam("file") MultipartFile file) {
        requireOrganizerRole(request);
        return ResponseEntity.ok(ApiResponse.success("ID template asset stored", fileStorageService.store(null, "id-template-asset", file)));
    }

    @PostMapping("/uploads/profile-photo")
    public ResponseEntity<ApiResponse<StoredFileResponse>> uploadProfilePhoto(HttpServletRequest request,
                                                                              @RequestParam("file") MultipartFile file) {
        requireAuthenticated(request);
        return ResponseEntity.ok(ApiResponse.success("Profile photo stored", fileStorageService.store(null, "profile-photo", file)));
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<StoredFileResponse>> getFile(HttpServletRequest request, @PathVariable UUID fileId) {
        requireFileAccess(request, fileId);
        return ResponseEntity.ok(ApiResponse.success(fileStorageService.find(fileId)));
    }

    @GetMapping(value = "/files/{fileId}/content", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> getFileContent(HttpServletRequest request, @PathVariable UUID fileId) {
        requireFileAccess(request, fileId);
        StoredFileResponse file = fileStorageService.find(fileId);
        FileStorageService.StoredFileContent content = fileStorageService.readContent(fileId);
        MediaType mediaType = file.contentType() == null || file.contentType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(file.contentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(content.content());
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<ApiResponse<StoredFileResponse>> deleteFile(HttpServletRequest request, @PathVariable UUID fileId) {
        requireFileAccess(request, fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted", fileStorageService.delete(fileId)));
    }

    private void requireOrganizerRole(HttpServletRequest request) {
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        if (role == AccountRole.ORGANIZER || role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        throw new ForbiddenException("Organizer or admin access required");
    }

    private void requireAuthenticated(HttpServletRequest request) {
        jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
    }

    private void requireFileAccess(HttpServletRequest request, UUID fileId) {
        UUID callerId = jwtService.extractUserIdFromBearer(request.getHeader("Authorization"));
        AccountRole role = jwtService.extractRoleFromBearer(request.getHeader("Authorization"));
        StoredFileResponse file = fileStorageService.find(fileId);
        if (file.ownerId() == null) {
            return; // no owner (e.g. public event poster / id template asset) -> any authenticated caller
        }
        if (callerId.equals(file.ownerId())) {
            return;
        }
        if (role == AccountRole.ADMIN || role == AccountRole.SUPER_ADMIN) {
            return;
        }
        throw new ForbiddenException("Access denied to file");
    }
}