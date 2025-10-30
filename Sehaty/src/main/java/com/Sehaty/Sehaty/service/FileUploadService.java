package com.Sehaty.Sehaty.service;


import com.Sehaty.Sehaty.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;



    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 🔹 استخراج اسم الملف والامتداد
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
            uploadParams.put("fl_attachment", false); // PDF يفتح مباشرة
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        String secureUrl = uploadResult.get("secure_url").toString();
        System.out.println("✅ Uploaded file URL: " + secureUrl);
        return secureUrl;
    }

}
