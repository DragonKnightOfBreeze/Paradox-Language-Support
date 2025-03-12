package icu.windea.pls.config.config

interface CwtPathMatchableConfig {
    /* Use Array<String> rather than Set<String> to optimize iterate performance */
    val pathPatterns: Array<String>
    /* Use Array<String> rather than Set<String> to optimize iterate performance */
    val paths: Array<String>
    val pathFile: String?
    val pathExtension: String?
    val pathStrict: Boolean
}
