package com.Sehaty.Sehaty;


import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.exception.BadRequestException;
import com.Sehaty.Sehaty.exception.ResourceNotFoundException;
import com.Sehaty.Sehaty.exception.UnauthorizedException;
import com.Sehaty.Sehaty.mapper.MedicalFileMapper;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.User;
import com.Sehaty.Sehaty.repository.MedicalFileRepository;
import com.Sehaty.Sehaty.repository.UserRepository;
import com.Sehaty.Sehaty.service.FileUploadService;
import com.Sehaty.Sehaty.service.MedicalFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class MedicalFileServiceTest {

    @Mock
    private MedicalFileRepository medicalFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private MedicalFileMapper medicalFileMapper;

    @InjectMocks
    private MedicalFileService medicalFileService;

    private User user;
    private UserDetails userDetails;
    private UUID userId;

    @BeforeEach
    void setup() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);

        userDetails = mock(UserDetails.class);
       lenient().when(userDetails.getUsername()).thenReturn(userId.toString());
    }

    // ==================== uploadFile ====================
    @Test
    void uploadFile_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("report.pdf");

        MedicalFileUploadRequestDTO requestDTO = new MedicalFileUploadRequestDTO();
        requestDTO.setCategory("RADIOLOGY");
        requestDTO.setSubCategory("CHEST");
        requestDTO.setDisplayName("Chest X-Ray");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(medicalFileRepository.countByOwner(user)).thenReturn(0L);
        when(fileUploadService.uploadFile(file)).thenReturn("http://file.url/report.pdf");

        MedicalFile savedFile = new MedicalFile();
        savedFile.setId(UUID.randomUUID());
        savedFile.setOwner(user);

        when(medicalFileRepository.save(any(MedicalFile.class))).thenReturn(savedFile);
        when(medicalFileMapper.toMedicalFileResponseDTO(savedFile)).thenReturn(new MedicalFileResponseDTO());

        MedicalFileResponseDTO result = medicalFileService.uploadFile(userDetails, file, requestDTO);

        assertNotNull(result);
    }

    @Test
    void uploadFile_EmptyFile_ThrowsException() {

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn("report.pdf");


        MedicalFileUploadRequestDTO requestDTO = new MedicalFileUploadRequestDTO();
        requestDTO.setCategory("RADIOLOGY");
        requestDTO.setSubCategory("CHEST");
        requestDTO.setDisplayName("Chest X-Ray");


        lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(user));



        assertThrows(BadRequestException.class,
                () -> medicalFileService.uploadFile(userDetails, file, requestDTO));
    }

    // ==================== getFileById ====================
    @Test
    void getFileById_Success() {
        UUID fileId = UUID.randomUUID();

        MedicalFile file = new MedicalFile();
        file.setId(fileId);
        file.setOwner(user);

        when(medicalFileRepository.findById(fileId)).thenReturn(Optional.of(file));
        when(medicalFileMapper.toMedicalFileResponseDTO(file)).thenReturn(new MedicalFileResponseDTO());

        MedicalFileResponseDTO result = medicalFileService.getFileById(fileId, userDetails);
        assertNotNull(result);
    }

    @Test
    void getFileById_NotOwned_ThrowsException() {
        UUID fileId = UUID.randomUUID();

        User owner = new User();
        owner.setId(UUID.randomUUID()); // different user
        MedicalFile file = new MedicalFile();
        file.setId(fileId);
        file.setOwner(owner);

        when(medicalFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThrows(UnauthorizedException.class,
                () -> medicalFileService.getFileById(fileId, userDetails));
    }

    // ==================== deleteFile ====================
    @Test
    void deleteFile_Success() {
        UUID fileId = UUID.randomUUID();

        MedicalFile file = new MedicalFile();
        file.setId(fileId);
        file.setOwner(user);

        when(medicalFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertDoesNotThrow(() -> medicalFileService.deleteFile(fileId, userDetails));
        verify(medicalFileRepository).delete(file);
    }

    @Test
    void deleteFile_NotOwned_ThrowsException() {
        UUID fileId = UUID.randomUUID();

        User owner = new User();
        owner.setId(UUID.randomUUID()); // different user
        MedicalFile file = new MedicalFile();
        file.setId(fileId);
        file.setOwner(owner);

        when(medicalFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        assertThrows(UnauthorizedException.class,
                () -> medicalFileService.deleteFile(fileId, userDetails));
    }

    // ==================== getAllFilesByUser ====================
    @Test
    void getAllFilesByUser_Success() {
        MedicalFile file = new MedicalFile();
        file.setId(UUID.randomUUID());
        file.setOwner(user);

        when(medicalFileRepository.findByOwnerId(userId)).thenReturn(List.of(file));
        when(medicalFileMapper.toMedicalFileResponseDTO(file)).thenReturn(new MedicalFileResponseDTO());

        List<MedicalFileResponseDTO> result = medicalFileService.getAllFilesByUser(userDetails);

        assertEquals(1, result.size());
    }

    @Test
    void getAllFilesByUser_NoFiles_ThrowsException() {
        when(medicalFileRepository.findByOwnerId(userId)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class,
                () -> medicalFileService.getAllFilesByUser(userDetails));
    }
}
