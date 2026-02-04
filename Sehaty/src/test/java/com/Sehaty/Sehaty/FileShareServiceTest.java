package com.Sehaty.Sehaty;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.exception.BadRequestException;
import com.Sehaty.Sehaty.exception.QRCodeGenerationException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.mapper.ShareRecordMapper;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.SharedRecords;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.SharedRecordRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.service.FileShareService;
import com.Sehaty.Sehaty.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FileShareServiceTest {

    private UserRepository userRepository;
    private MedicalFileRepository medicalFileRepository;
    private SharedRecordRepository sharedRecordRepository;
    private FileUploadService fileUploadService;
    private ShareRecordMapper shareRecordMapper;
    private MedicalFileMapper medicalFileMapper;

    private FileShareService fileShareService;

    private UUID userId;
    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        medicalFileRepository = mock(MedicalFileRepository.class);
        sharedRecordRepository = mock(SharedRecordRepository.class);
        fileUploadService = mock(FileUploadService.class);
        shareRecordMapper = mock(ShareRecordMapper.class);
        medicalFileMapper = mock(MedicalFileMapper.class);

        fileShareService = new FileShareService(
                userRepository,
                medicalFileRepository,
                sharedRecordRepository,
                fileUploadService,
                shareRecordMapper,
                medicalFileMapper
        );

        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("Ali");

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userId.toString());
    }

    // --------------------------------------------------------------------
    // createShareSession TESTS
    // --------------------------------------------------------------------

    @Test
    @DisplayName("Create share: user not found")
    void createShare_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fileShareService.createShareSession(userDetails, List.of(UUID.randomUUID())));
    }

    @Test
    @DisplayName("Create share: files not found")
    void createShare_NoFiles() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(medicalFileRepository.findAllById(any())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> fileShareService.createShareSession(userDetails, List.of(UUID.randomUUID())));
    }

    @Test
    @DisplayName("Create share: files not owned by user")
    void createShare_FilesNotOwned() {
        MedicalFile file = new MedicalFile();
        file.setId(UUID.randomUUID());

        User owner2 = new User();
        owner2.setId(UUID.randomUUID());
        file.setOwner(owner2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(medicalFileRepository.findAllById(any())).thenReturn(List.of(file));

        assertThrows(
                BadRequestException.class,
                () -> fileShareService.createShareSession(
                        userDetails,
                        List.of(file.getId())
                )
        );
    }

    @Test
    @DisplayName("Create share: success")
    void createShare_Success() {
        MedicalFile file = new MedicalFile();
        file.setId(UUID.randomUUID());
        file.setOwner(user);

        SharedRecords record = new SharedRecords();
        record.setId(UUID.randomUUID());
        record.setUser(user);
        record.setSharedFiles(List.of(file));
        record.setStatus(SharedRecords.ShareStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(medicalFileRepository.findAllById(any())).thenReturn(List.of(file));
        when(sharedRecordRepository.save(any())).thenReturn(record);

        SharedRecordDTO dto = new SharedRecordDTO();
        dto.setId(record.getId());
        when(shareRecordMapper.toDTO(any())).thenReturn(dto);

        SharedRecordDTO result = fileShareService.createShareSession(userDetails, List.of(file.getId()));

        assertNotNull(result);
        assertEquals(record.getId(), result.getId());
    }

    // --------------------------------------------------------------------
    // getFilesByQrCode TESTS
    // --------------------------------------------------------------------

    @Test
    @DisplayName("Get files by QR: QR not found")
    void getFilesByQr_QRNotFound() {
        when(sharedRecordRepository.findByQrCode("x")).thenReturn(Optional.empty());

        assertThrows(QRCodeGenerationException.class,
                () -> fileShareService.getFilesByQrCode("x"));
    }

    @Test
    @DisplayName("Get files by QR: success")
    void getFilesByQr_Success() {
        MedicalFile file = new MedicalFile();
        SharedRecords record = new SharedRecords();
        record.setSharedFiles(List.of(file));

        when(sharedRecordRepository.findByQrCode("qr")).thenReturn(Optional.of(record));
        when(medicalFileMapper.toMedicalFileResponseDTO(any())).thenReturn(new MedicalFileResponseDTO());

        List<MedicalFileResponseDTO> result = fileShareService.getFilesByQrCode("qr");

        assertEquals(1, result.size());
    }

    // --------------------------------------------------------------------
    // accessShare TESTS
    // --------------------------------------------------------------------

    @Test
    @DisplayName("Access share: not found")
    void accessShare_NotFound() {
        when(sharedRecordRepository.findByQrCode("x")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fileShareService.accessShare("x"));
    }

    @Test
    @DisplayName("Access share: revoked")
    void accessShare_Revoked() {
        SharedRecords record = new SharedRecords();
        record.setStatus(SharedRecords.ShareStatus.REVOKED);

        when(sharedRecordRepository.findByQrCode("x")).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class,
                () -> fileShareService.accessShare("x"));
    }

    @Test
    @DisplayName("Access share: expired session")
    void accessShare_Expired() {
        SharedRecords record = new SharedRecords();
        record.setStatus(SharedRecords.ShareStatus.ACTIVE);
        record.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(sharedRecordRepository.findByQrCode("x")).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class,
                () -> fileShareService.accessShare("x"));
    }

    @Test
    @DisplayName("Access share: success")
    void accessShare_Success() {
        SharedRecords record = new SharedRecords();
        record.setStatus(SharedRecords.ShareStatus.ACTIVE);
        record.setUser(user);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(sharedRecordRepository.findByQrCode("x")).thenReturn(Optional.of(record));

        SharedRecordDTO dto = new SharedRecordDTO();
        when(shareRecordMapper.toDTO(any())).thenReturn(dto);

        SharedRecordDTO result = fileShareService.accessShare("x");

        assertNotNull(result);
    }

    // --------------------------------------------------------------------
    // revokeShare TESTS
    // --------------------------------------------------------------------

    @Test
    @DisplayName("Revoke: share not found")
    void revokeShare_NotFound() {
        when(sharedRecordRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fileShareService.revokeShare(UUID.randomUUID(), userDetails));
    }

    @Test
    @DisplayName("Revoke: not your session")
    void revokeShare_NotOwner() {
        SharedRecords record = new SharedRecords();
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());
        record.setUser(anotherUser);

        when(sharedRecordRepository.findById(any())).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class,
                () -> fileShareService.revokeShare(UUID.randomUUID(), userDetails));
    }

    @Test
    @DisplayName("Revoke: already revoked")
    void revokeShare_AlreadyRevoked() {
        SharedRecords record = new SharedRecords();
        record.setUser(user);
        record.setStatus(SharedRecords.ShareStatus.REVOKED);

        when(sharedRecordRepository.findById(any())).thenReturn(Optional.of(record));

        assertThrows(BadRequestException.class,
                () -> fileShareService.revokeShare(UUID.randomUUID(), userDetails));
    }

    @Test
    @DisplayName("Revoke: success")
    void revokeShare_Success() {
        SharedRecords record = new SharedRecords();
        record.setUser(user);
        record.setStatus(SharedRecords.ShareStatus.ACTIVE);

        when(sharedRecordRepository.findById(any())).thenReturn(Optional.of(record));
        when(sharedRecordRepository.save(any())).thenReturn(record);
        when(shareRecordMapper.toDTO(any())).thenReturn(new SharedRecordDTO());

        SharedRecordDTO dto = fileShareService.revokeShare(UUID.randomUUID(), userDetails);

        assertNotNull(dto);
    }

    // --------------------------------------------------------------------
    // getUserSessions TESTS
    // --------------------------------------------------------------------

    @Test
    @DisplayName("Get user sessions: user not found")
    void getSessions_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> fileShareService.getUserSessions(userDetails));
    }

    @Test
    @DisplayName("Get user sessions: success")
    void getSessions_Success() {
        SharedRecords active = new SharedRecords();
        active.setStatus(SharedRecords.ShareStatus.ACTIVE);
        active.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        SharedRecords expired = new SharedRecords();
        expired.setStatus(SharedRecords.ShareStatus.ACTIVE);
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(10));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sharedRecordRepository.findByUser(user))
                .thenReturn(List.of(active, expired));

        when(shareRecordMapper.toDTO(any())).thenReturn(new SharedRecordDTO());

        Map<String, List<SharedRecordDTO>> sessions =
                fileShareService.getUserSessions(userDetails);

        assertEquals(1, sessions.get("active").size());
        assertEquals(1, sessions.get("expired").size());
    }
}
