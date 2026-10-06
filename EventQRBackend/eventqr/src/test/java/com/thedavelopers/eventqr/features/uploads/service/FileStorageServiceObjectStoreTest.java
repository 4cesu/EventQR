package com.thedavelopers.eventqr.features.uploads.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.thedavelopers.eventqr.features.uploads.model.dto.StoredFileResponse;
import com.thedavelopers.eventqr.features.uploads.model.entity.StoredFile;
import com.thedavelopers.eventqr.features.uploads.repository.StoredFileRepository;

class FileStorageServiceObjectStoreTest {

    private final Map<String, byte[]> bucket = new HashMap<>();
    private StoredFileRepository repository;
    private FileContentStore store;
    private StoredFile saved;
    private byte[] png;

    @BeforeEach
    void setUp() throws Exception {
        BufferedImage image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        png = out.toByteArray();

        repository = mock(StoredFileRepository.class);
        when(repository.save(any(StoredFile.class))).thenAnswer(invocation -> {
            saved = invocation.getArgument(0);
            return saved;
        });
        store = new FileContentStore() {
            @Override
            public void put(String key, String contentType, byte[] content) {
                bucket.put(key, content);
            }

            @Override
            public byte[] get(String key) {
                return bucket.get(key);
            }

            @Override
            public void delete(String key) {
                bucket.remove(key);
            }
        };
    }

    private MockMultipartFile upload() {
        return new MockMultipartFile("file", "me.png", "image/png", png);
    }

    @Test
    void withAnObjectStoreTheBytesGoToTheBucketAndNotTheDatabase() {
        FileStorageService service = new FileStorageService(repository, store);

        StoredFileResponse response = service.store(UUID.randomUUID(), "profile-photo", upload());

        assertThat(saved.getContent()).isNull();
        assertThat(saved.getStorageKey()).startsWith("profile-photo/");
        assertThat(bucket).containsOnlyKeys(saved.getStorageKey());
        assertThat(bucket.get(saved.getStorageKey())).isEqualTo(png);
        assertThat(response.contentBase64()).isNotBlank();
    }

    @Test
    void withoutAnObjectStoreBehaviourIsUnchanged() {
        FileStorageService service = new FileStorageService(repository);

        service.store(UUID.randomUUID(), "profile-photo", upload());

        assertThat(saved.getContent()).isEqualTo(png);
        assertThat(saved.getStorageKey()).isNull();
        assertThat(bucket).isEmpty();
    }

    @Test
    void anObjectStoredFileIsReadBackFromTheBucket() {
        FileStorageService service = new FileStorageService(repository, store);
        service.store(UUID.randomUUID(), "profile-photo", upload());
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(saved));

        FileStorageService.StoredFileContent content = service.readContent(id);

        assertThat(content.content()).isEqualTo(png);
    }

    @Test
    void aDatabaseStoredFileStillReadsAfterSwitchingToTheObjectStore() {
        StoredFile legacy = new StoredFile();
        legacy.setContent(png);
        legacy.setContentType("image/png");
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(legacy));
        FileStorageService service = new FileStorageService(repository, store);

        assertThat(service.readContent(id).content()).isEqualTo(png);
    }

    @Test
    void deletingRemovesTheObjectToo() {
        FileStorageService service = new FileStorageService(repository, store);
        service.store(UUID.randomUUID(), "profile-photo", upload());
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(saved));

        service.delete(id);

        assertThat(bucket).isEmpty();
        verify(repository).delete(saved);
    }

    @Test
    void aFailedDatabaseInsertDoesNotLeaveAnOrphanObject() {
        doThrow(new IllegalStateException("db down")).when(repository).save(any(StoredFile.class));
        FileStorageService service = new FileStorageService(repository, store);

        assertThatThrownBy(() -> service.store(UUID.randomUUID(), "profile-photo", upload()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bucket).isEmpty();
    }

    @Test
    void aFailedUploadToTheBucketNeverCreatesADatabaseRow() {
        FileContentStore failing = mock(FileContentStore.class);
        doThrow(new IllegalStateException("s3 down")).when(failing).put(any(), any(), any());
        FileStorageService service = new FileStorageService(repository, failing);

        assertThatThrownBy(() -> service.store(UUID.randomUUID(), "profile-photo", upload()))
                .isInstanceOf(IllegalStateException.class);

        verify(repository, never()).save(any());
    }
}
