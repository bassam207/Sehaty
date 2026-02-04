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

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between User entities and DTOs.
 */
@Component
@RequiredArgsConstructor
public class UserMapper {

    private final MedicalFileMapper medicalFileMapper;

    /**
     * Converts a UserRequestDTO to a User entity.
     *
     * @param userRequestDTO The DTO containing user registration data.
     * @return The User entity.
     */
    public User convertToUser(UserRequestDTO userRequestDTO) {
        User user = new User();

        user.setName(userRequestDTO.getName());
        user.setEmail(userRequestDTO.getEmail());
        user.setGender(userRequestDTO.getGender());
        user.setDateOfBirth(userRequestDTO.getDateOfBirth());


        return user;
    }


    /**
     * Converts a User entity to a UserResponseDTO.
     * Includes calculating age and mapping associated medical files.
     *
     * @param user The User entity.
     * @return The UserResponseDTO.
     */
    public UserResponseDTO convertTOUserResponseDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();

        userResponseDTO.setId(user.getId());
        userResponseDTO.setName(user.getName());
        userResponseDTO.setGender(user.getGender());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setDateOfBirth(user.getDateOfBirth());

        if (user.getDateOfBirth() != null) {
            int age = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();

            userResponseDTO.setAge(age);

        }

        List<MedicalFileResponseDTO> fileDTOs = Optional.ofNullable(user.getFiles())
                .orElse(Collections.emptyList())
                .stream()
                .map(medicalFileMapper::toMedicalFileResponseDTO)
                .toList();

        userResponseDTO.setFiles(fileDTOs);


        return userResponseDTO;

    }
}
