package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.service.FileShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
public class FileShareByWebController {

    private final FileShareService fileShareService;

    @GetMapping("/by-qr")
    public String openShare(@RequestParam String qrCode, Model model) {
        try {
            // جلب بيانات الجلسة
            SharedRecordDTO sharedRecord = fileShareService.accessShare(qrCode);

            // تقسيم الملفات حسب الفئة
            Map<String, List<MedicalFileResponseDTO>> categorizedFiles = categorizeFiles(
                    sharedRecord.getSharedFiles()
            );

            // إضافة البيانات للـ model
            model.addAttribute("patientName", sharedRecord.getUserName());
            model.addAttribute("qrCode", sharedRecord.getQrCode());
            model.addAttribute("timeRemaining", sharedRecord.getTimeRemaining());
            model.addAttribute("expiresAt", sharedRecord.getExpiresAt());
            model.addAttribute("status", sharedRecord.getStatus());

            // الملفات المقسمة
            model.addAttribute("xrayFiles", categorizedFiles.getOrDefault("أشعة", List.of()));
            model.addAttribute("labFiles", categorizedFiles.getOrDefault("تحاليل", List.of()));
            model.addAttribute("prescriptionFiles", categorizedFiles.getOrDefault("تقارير و روشتات", List.of()));

            // إحصائيات
            model.addAttribute("xrayCount", categorizedFiles.getOrDefault("أشعة", List.of()).size());
            model.addAttribute("labCount", categorizedFiles.getOrDefault("تحاليل", List.of()).size());
            model.addAttribute("prescriptionCount", categorizedFiles.getOrDefault("تقارير و روشتات", List.of()).size());

            for (MedicalFileResponseDTO file : sharedRecord.getSharedFiles()) {
                System.out.println(
                        "DisplayName: " + file.getDisplayName() +
                                ", Category: " + file.getCategory() +
                                ", URL: " + file.getUrl()
                );
            }
            System.out.println(
                    "Categories موجودة: " + sharedRecord.getSharedFiles().stream()
                            .map(MedicalFileResponseDTO::getCategory)
                            .collect(Collectors.toSet())
            );
            return "share-view";

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error-share";
        }
    }

    private Map<String, List<MedicalFileResponseDTO>> categorizeFiles(
            List<MedicalFileResponseDTO> files) {

        return files.stream()
                .collect(Collectors.groupingBy(
                        file -> file.getCategory() != null ? file.getCategory() : "أخرى"
                ));
    }
}
