package icu.windea.pls.config.config

import com.intellij.psi.PsiElement
import icu.windea.pls.config.annotations.FromMember

/**
 * 可匹配文件路径的规则的统一抽象。
 *
 * @property paths 允许的父目录（可多个）。
 * @property pathFile 允许的文件名（单个）。
 * @property pathExtension 允许的文件扩展名（单个）。
 * @property pathStrict 是否严格匹配。严格匹配意味着不匹配子目录中的文件。
 * @property pathPatterns 允许的路径模式（可多个，使用 ANT 表达式）。
 */
interface CwtFilePathMatchableConfig<T : PsiElement> : CwtConfig<T> {
    @FromMember("path: string", multiple = true)
    val paths: Set<String>
    @FromMember("path_file: string?")
    val pathFile: String?
    @FromMember("path_extension: string?")
    val pathExtension: String?
    @FromMember("path_strict: boolean", defaultValue = "no")
    val pathStrict: Boolean
    @FromMember("path_pattern: string", multiple = true)
    val pathPatterns: Set<String>
}

