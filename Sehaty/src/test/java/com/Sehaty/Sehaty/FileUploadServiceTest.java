package com.Sehaty.Sehaty;

import com.Sehaty.Sehaty.service.FileUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileUploadServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private FileUploadService fileUploadService;

    @BeforeEach
    void setup() {
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    @DisplayName("Upload empty file should throw exception")
    void uploadFile_emptyFile_throwsException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> fileUploadService.uploadFile(file));
    }

    @Test
    @DisplayName("Upload PDF should set fl_attachment=false")
    void uploadPdf_setsAttachmentFalse() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("report.pdf");
        when(file.getBytes()).thenReturn("dummy".getBytes());

        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://cloudinary.com/test.pdf");

        when(uploader.upload(any(), anyMap())).thenReturn(result);

        String url = fileUploadService.uploadFile(file);

        assertEquals("https://cloudinary.com/test.pdf", url);

        verify(uploader).upload(any(), argThat(params ->
                params.containsKey("fl_attachment") &&
                        params.get("fl_attachment").equals(false)
        ));
    }

    @Test
    @DisplayName("Upload file returns secure_url")
    void uploadFile_returnsUrl() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("image.png");
        when(file.getBytes()).thenReturn("content".getBytes());

        Map<String, Object> result = new HashMap<>();
        result.put("secure_url", "https://cloudinary.com/image.png");

        when(uploader.upload(any(), anyMap())).thenReturn(result);

        String url = fileUploadService.uploadFile(file);

        assertEquals("https://cloudinary.com/image.png", url);
    }
}
