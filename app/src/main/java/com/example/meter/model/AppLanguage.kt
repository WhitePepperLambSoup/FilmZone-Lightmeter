package com.example.meter.model

/**
 * Supported UI Languages
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    SIMPLIFIED_CHINESE("zh-CN", "Simplified Chinese", "简体中文"),
    TRADITIONAL_CHINESE("zh-TW", "Traditional Chinese", "繁體中文"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    FRENCH("fr", "French", "Français"),
    SPANISH("es", "Spanish", "Español"),
    GERMAN("de", "German", "Deutsch"),
    RUSSIAN("ru", "Russian", "Русский"),
    ARABIC("ar", "Arabic", "العربية");

    companion object {
        val DEFAULT = SIMPLIFIED_CHINESE

        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
