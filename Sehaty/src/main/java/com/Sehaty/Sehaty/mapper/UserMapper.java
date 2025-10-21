package com.Sehaty.Sehaty.mapper;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.MedicalFileUploadRequestDTO;
import com.Sehaty.Sehaty.dto.UserRequestDTO;
import com.Sehaty.Sehaty.dto.UserResponseDTO;
import com.Sehaty.Sehaty.model.MedicalFile;
import com.Sehaty.Sehaty.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

private final MedicalFileMapper medicalFileMapper;
    public User convertToUser(UserRequestDTO userRequestDTO)
    {
        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());

        return user;
    }




    public UserResponseDTO convertTOUserResponseDTO(User user)
    {
        UserResponseDTO userResponseDTO = new UserResponseDTO();

        userResponseDTO.setName(user.getName());
        userResponseDTO.setEmail(user.getEmail());

        List<MedicalFileResponseDTO> fileDTOs = Optional.ofNullable(user.getFiles())
                .orElse(Collections.emptyList())
                .stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();

        userResponseDTO.setFiles(fileDTOs);


        return userResponseDTO;

    }

/**    private static MedicalFileUploadRequestDTO mapMedicalFile(MedicalFile file) {
        return Optional.ofNullable(file)
                .map(f -> {
                    MedicalFileUploadRequestDTO dto = new MedicalFileUploadRequestDTO();
                    dto.setFileName(Optional.ofNullable(f.getFileName()).orElse("Unnamed File"));
                    dto.setCategory(Optional.ofNullable(f.getCategory()).orElse("Unnamed category"));
                    dto.setSubCategory(Optional.ofNullable(f.getSubCategory()).orElse("Unnamed subcategory"));
                    dto.setUploadedAt(f.getUploadedAt());
                    dto.setUrl(Optional.ofNullable(f.getUrl()).orElse(""));
                    return dto;
                })
                .orElseGet(MedicalFileUploadRequestDTO::new); // return empty dto if file is null
    }*/
}
