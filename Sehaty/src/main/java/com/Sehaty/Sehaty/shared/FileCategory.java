package com.Sehaty.Sehaty.shared;

import java.util.Map;
import java.util.Optional;

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
     * يحاول يطابق النص العربي مع أي فئة معروفة
     */
    public static Optional<FileCategory> fromArabic(String input) {
        if (input == null || input.isBlank()) return Optional.empty();
        String normalized = normalizeArabic(input);

        for (FileCategory cat : values()) {
            if (normalizeArabic(cat.arabicName).equals(normalized))
                return Optional.of(cat);

            // لو النص فيه كلمة زي "اشعه" ضمنه
            if (normalized.contains(normalizeArabic(cat.arabicName)))
                return Optional.of(cat);
        }

        if (normalized.contains("روشتات") || normalized.contains("تقارير"))
            return Optional.of(REPORTS);
        return Optional.empty();
    }


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
     * توحيد النص العربي قبل المقارنة
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
