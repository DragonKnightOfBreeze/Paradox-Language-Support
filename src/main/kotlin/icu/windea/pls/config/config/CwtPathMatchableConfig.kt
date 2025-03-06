package icu.windea.pls.config.config

interface CwtPathMatchableConfig {
    val pathPatterns: Set<String>
    val paths: Set<String>
    val pathFile: String?
    val pathExtension: String?
    val pathStrict: Boolean
}
