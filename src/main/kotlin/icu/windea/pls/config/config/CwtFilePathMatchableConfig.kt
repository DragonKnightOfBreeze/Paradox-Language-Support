package icu.windea.pls.config.config

interface CwtFilePathMatchableConfig {
    val paths: Set<String>
    val pathFile: String?
    val pathExtension: String?
    val pathStrict: Boolean
    val pathPatterns: Set<String>

    val filePathPatterns: Set<String>
    val filePathPatternsForPriority: Set<String>
}
