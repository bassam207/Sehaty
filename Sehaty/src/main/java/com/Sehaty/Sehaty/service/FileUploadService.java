package com.Sehaty.Sehaty.service;


import com.Sehaty.Sehaty.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException
    {
        if (file.isEmpty())
        {
            throw new BadRequestException("File cannot be empty");
        }

      /**  Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap
                (
                        "resource_type","auto"
                ));*/
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        System.out.println(uploadResult);


        return uploadResult.get("secure_url").toString();
    }


}
