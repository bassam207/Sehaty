package com.Sehaty.Sehaty.shared;

import java.util.Map;
import java.util.Optional;

/**
 * Enumeration representing categories of medical files.
 * Includes mapping for Arabic display names and subcategories.
 */
public enum FileCategory {

    RADIOLOGY("أشعة", Map.ofEntries(
            Map.entry("أشعة عادية", "X-RAY"),
            Map.entry("اشعة عادية", "X-RAY"),
            Map.entry("اشعه عادية", "X-RAY"),
            Map.entry("مقطعية", "CT"),
            Map.entry("اشعة مقطعية", "CT"),
            Map.entry("رنين", "MRI"),
            Map.entry("رنين مغناطيسي", "MRI"),
            Map.entry("سونار", "ULTRASOUND"),
            Map.entry("أشعة تليفزيونية", "ULTRASOUND")
    )),

    LABS("تحاليل", Map.ofEntries(
            Map.entry("تحليل دم", "CBC"),
            Map.entry("صورة دم", "CBC"),
            Map.entry("تحليل سكر", "BLOOD_SUGAR"),
            Map.entry("سكر بالدم", "BLOOD_SUGAR"),
            Map.entry("وظائف كبد", "LFT"),
            Map.entry("وظائف كلى", "RFT"),
            Map.entry("بول", "URINE")
    )),

    REPORTS("تقارير و روشتات", Map.ofEntries(
            Map.entry("روشتة", "PRESCRIPTION"),
            Map.entry("تقرير طبي", "MEDICAL_REPORT"),
            Map.entry("متابعة", "FOLLOW_UP")
    ));

    private final String arabicName;
    private final Map<String, String> subcategories;

    FileCategory(String arabicName, Map<String, String> subcategories) {
        this.arabicName = arabicName;
        this.subcategories = subcategories;
    }

    public String getArabicName() {
        return arabicName;
    }

    public Map<String, String> getSubcategories() {
        return subcategories;
    }

    /**
     * Tries to match an Arabic string to a known FileCategory.
     *
     * @param input The Arabic string input.
     * @return Optional containing the matching FileCategory, or empty if not found.
     */
    public static Optional<FileCategory> fromArabic(String input) {
        if (input == null || input.isBlank()) return Optional.empty();
        String normalized = normalizeArabic(input);

        for (FileCategory cat : values()) {
            if (normalizeArabic(cat.arabicName).equals(normalized))
                return Optional.of(cat);

            // Check if the input contains a keyword like "اشعه"
            if (normalized.contains(normalizeArabic(cat.arabicName)))
                return Optional.of(cat);
        }

        if (normalized.contains("روشتات") || normalized.contains("تقارير"))
            return Optional.of(REPORTS);
        return Optional.empty();
    }


    /**
     * Resolves a subcategory key from an input string.
     *
     * @param input The subcategory input string.
     * @return The resolved subcategory key, or "OTHER" if not found.
     */
    public String resolveSubcategoryKey(String input) {
        if (input == null || input.isBlank()) return "OTHER";
        String normalized = normalizeArabic(input);

        for (Map.Entry<String, String> entry : subcategories.entrySet()) {
            if (normalizeArabic(entry.getKey()).equals(normalized) ||
                    normalized.contains(normalizeArabic(entry.getKey())))
                return entry.getValue();
        }
        return "OTHER";
    }

    /**
     * Normalizes Arabic text for comparison.
     * Removes diacritics and unifies alef forms.
     *
     * @param text The text to normalize.
     * @return The normalized text.
     */
    private static String normalizeArabic(String text) {
        return text.toLowerCase()
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ة", "ه")
                .trim();
    }
}
