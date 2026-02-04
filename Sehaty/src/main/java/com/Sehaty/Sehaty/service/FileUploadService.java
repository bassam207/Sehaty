package com.Sehaty.Sehaty.service;


import com.Sehaty.Sehaty.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for handling file uploads to Cloudinary.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;


    /**
     * Uploads a file to Cloudinary.
     *
     * @param file The file to upload.
     * @return The secure URL of the uploaded file.
     * @throws IOException if an I/O error occurs during upload.
     * @throws IllegalArgumentException if the file is empty.
     */
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFileName = file.getOriginalFilename();
        String fileNameWithoutExt = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.') + 1);


        Map uploadParams = new HashMap<>();
        uploadParams.put("resource_type", "raw");
        uploadParams.put("public_id", fileNameWithoutExt);
        uploadParams.put("format", extension);
        uploadParams.put("use_filename", true);
        uploadParams.put("unique_filename", false);

        if (extension.equalsIgnoreCase("pdf")) {
            uploadParams.put("fl_attachment", false); // PDF opens directly
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        String secureUrl = uploadResult.get("secure_url").toString();
        log.info("✅ Uploaded file URL: {}", secureUrl);
        return secureUrl;
    }

}
