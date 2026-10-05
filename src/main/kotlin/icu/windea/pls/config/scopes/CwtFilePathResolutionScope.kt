package icu.windea.pls.config.scopes

import icu.windea.pls.config.CwtConfigConstants
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull

@Suppress("unused")
@Optimized
interface CwtFilePathResolutionScope {
    /**
     * 解析规则文件中的（文件）路径（同时进行规范化和优化处理）。
     *
     * 例如，`game/common/on_actions` 将会被规范化为 `common/on_actions`。
     */
    fun String.resolvePath(): String? {
        if (this.isEmpty()) return null
        val prefixes = CwtConfigConstants.pathPrefixes
        val result = prefixes.firstNotNullOfOrNull { removePrefixOrNull(it) } ?: this
        return result.normalizePath().orNull()?.optimized()
    }

    /**
     * 解析规则文件中的（文件）路径扩展名（同时进行规范化和优化处理）。
     *
     * 例如，`.txt` 将会被规范化为 `txt`。
     */
    fun String.resolvePathExtension(): String? {
        if (this.isEmpty()) return null
        val result = removePrefix(".")
        return result.orNull()?.optimized()
    }

    companion object INSTANCE : CwtFilePathResolutionScope
}
