package com.Sehaty.Sehaty.controller;

import com.Sehaty.Sehaty.dto.MedicalFileResponseDTO;
import com.Sehaty.Sehaty.dto.SharedRecordDTO;
import com.Sehaty.Sehaty.service.FileShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling web-based file sharing views.
 * Renders HTML pages for displaying shared files.
 */
@Controller
@RequestMapping("/share")
@RequiredArgsConstructor
@Slf4j
public class FileShareByWebController {

    private final FileShareService fileShareService;

    /**
     * Displays the shared files page using a UUID in the path.
     * URL: /share/{uuid}
     *
     * @param uuid The UUID of the share session.
     * @param model The Spring MVC model.
     * @return The name of the view to render.
     */
    @GetMapping("/{uuid}")
    public String openShareByPath(@PathVariable String uuid, Model model) {
        try {
            log.info("=== Loading share session: {} ===", uuid);

            SharedRecordDTO sharedRecord = fileShareService.accessShare(uuid);

            if (sharedRecord == null) {
                model.addAttribute("errorMessage", "Share session not found or expired");
                return "error-share";
            }

            Map<String, List<MedicalFileResponseDTO>> categorizedFiles = categorizeFiles(
                    sharedRecord.getSharedFiles()
            );

            model.addAttribute("patientName", sharedRecord.getUserName());
            model.addAttribute("qrCode", sharedRecord.getQrCode());
            model.addAttribute("timeRemaining", sharedRecord.getTimeRemaining());
            model.addAttribute("expiresAt", sharedRecord.getExpiresAt());
            model.addAttribute("status", sharedRecord.getStatus());

            model.addAttribute("xrayFiles", categorizedFiles.getOrDefault("أشعة", List.of()));
            model.addAttribute("labFiles", categorizedFiles.getOrDefault("تحاليل", List.of()));
            model.addAttribute("prescriptionFiles", categorizedFiles.getOrDefault("تقارير و روشتات", List.of()));

            model.addAttribute("xrayCount", categorizedFiles.getOrDefault("أشعة", List.of()).size());
            model.addAttribute("labCount", categorizedFiles.getOrDefault("تحاليل", List.of()).size());
            model.addAttribute("prescriptionCount", categorizedFiles.getOrDefault("تقارير و روشتات", List.of()).size());

            log.debug("=== Loaded files: {} files ===", sharedRecord.getSharedFiles().size());
            for (MedicalFileResponseDTO file : sharedRecord.getSharedFiles()) {
                log.trace(
                        "DisplayName: {}, Category: {}, URL: {}",
                        file.getDisplayName(),
                        file.getCategory(),
                        file.getUrl()
                );
            }
            log.debug(
                    "Existing Categories: {}", sharedRecord.getSharedFiles().stream()
                            .map(MedicalFileResponseDTO::getCategory)
                            .collect(Collectors.toSet())
            );

            return "share-view";

        } catch (Exception e) {
            log.error("Error loading share session: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", e.getMessage());
            return "error-share";
        }
    }

    /**
     * Displays the shared files page using a qrCode as a query parameter.
     * URL: /share/by-qr?qrCode=...
     *
     * @param qrCode The QR code of the share session.
     * @param model The Spring MVC model.
     * @return The name of the view to render.
     */
    @GetMapping("/by-qr")
    public String openShare(@RequestParam String qrCode, Model model) {
        return openShareByPath(qrCode, model);
    }

    /**
     * Categorizes files by their category.
     *
     * @param files The list of files to categorize.
     * @return A map where keys are categories and values are lists of files.
     */
    private Map<String, List<MedicalFileResponseDTO>> categorizeFiles(
            List<MedicalFileResponseDTO> files) {

        if (files == null || files.isEmpty()) {
            return Map.of(
                    "أشعة", List.of(),
                    "تحاليل", List.of(),
                    "تقارير و روشتات", List.of()
            );
        }

        return files.stream()
                .collect(Collectors.groupingBy(
                        file -> file.getCategory() != null ? file.getCategory() : "أخرى"
                ));
    }
}
